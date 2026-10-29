package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantIntentIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.mapper.toIntentExecution
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.model.IntentResolvedCount
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantQueuedMutationsRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.utils.reportingMessage
import com.gdavidpb.tuindice.base.domain.utils.reportingName
import com.gdavidpb.tuindice.base.domain.utils.rootCause
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.MutatingIntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.single

/**
 * The order every confirmation follows, so the use cases that run an intent differ only in the port they call. The
 * intent must be open and of the kind the use case confirms. The server decides, the deadline included: an expiry or a
 * conflict it answers is reported without running anything, and the clock of the phone expires no intent by itself. An
 * accepted intent runs on its port, and its outcome is reported with the mutations of its binding and marked resolved.
 * A decision repeated after its answer was lost is answered as decided, so it runs; a conflict is final. An outcome
 * report stays in the outbox until the server settles it, and a flush sends it again when it was not answered. Nothing
 * here catches: a port answers FAILED instead of throwing.
 *
 * A report names how each mutation of a batch ended. An intent that answered QUEUED is not left there: its queued
 * mutations are followed in the app session, and once they settle (before the intent's deadline) the final outcome goes
 * through the outbox as one more report, and the intent is marked resolved with it.
 *
 * The confirmation belongs to the session ([sessionCoroutineScope]), not to the caller: closing the sheet that asked
 * does not cancel its decision, its run or its report. While a confirmation goes on, its intent is taken: a second
 * confirmation of it is refused as an intent that is not open, so the run happens once.
 */
class AssistantIntentConfirmer(
	private val conversationRepository: AssistantConversationRepository,
	private val intentRepository: AssistantIntentRepository,
	private val outboxRepository: AssistantIntentOutboxRepository,
	private val queuedMutationsRepository: AssistantQueuedMutationsRepository,
	private val sessionCoroutineScope: SessionCoroutineScope,
	private val reportingRepository: ReportingRepository
) {
	/**
	 * Confirms the intent of [params] as the kind [narrow] picks, and runs it with [execute]. With [reportsOutcome] false
	 * the success is not reported, because the server applies it; an expiry or a conflict is always reported.
	 */
	suspend fun <A : Any> confirm(
		params: ConfirmIntentParams,
		narrow: (IntentArgs) -> A?,
		reportsOutcome: Boolean = true,
		execute: suspend (args: A, intent: Intent) -> IntentExecution
	): OutcomeKind {
		// The session the confirmation belongs to: what it writes after its request is kept only for that session.
		val sessionMark = conversationRepository.sessionMark()
		val intent = conversationRepository.currentConversation().openIntents[params.intentId]
			?: throw AssistantIntentIllegalArgumentException(AssistantIntentError.IntentNotOpen)

		val args = resolveArgs(intent, params.editedArgs, narrow)
		// Only edited args of the confirmed kind are sent: any other edit was ignored, so the server never sees it.
		val editedArgs = params.editedArgs?.takeIf { edited -> narrow(edited) != null }

		// An intent whose confirmation goes on is taken: a second confirmation of it is not a second run.
		if (!outboxRepository.claimConfirmation(intent.id, sessionMark)) {
			throw AssistantIntentIllegalArgumentException(AssistantIntentError.IntentNotOpen)
		}

		val decision = IntentDecision(intentId = intent.id, accepted = true, editedArgs = editedArgs)

		// The confirmation runs in the session: the caller may leave, and the decision, the run and the report still
		// happen. The claim ends with the confirmation, however it ends.
		val confirmation = sessionCoroutineScope.async {
			when (intentRepository.decide(decision)) {
				IntentDecisionResult.Accepted -> executeAccepted(intent, args, execute, reportsOutcome, sessionMark)
				IntentDecisionResult.Expired -> closeWithoutRunning(intent, OutcomeKind.EXPIRED, sessionMark)
				IntentDecisionResult.Conflict -> closeWithoutRunning(intent, OutcomeKind.CONFLICT, sessionMark)
			}
		}
		confirmation.invokeOnCompletion { outboxRepository.releaseConfirmation(intent.id, sessionMark) }

		// The caller waits for the confirmation. A caller that leaves before it ends does not await it, so a failure of the
		// confirmation is reported here, as the about module's writes are. A failure the caller awaits reaches the caller,
		// and the use case that asked reports it, once.
		return flow { emit(confirmation.await()) }
			.onCompletion { cause ->
				if (cause is CancellationException) {
					confirmation.invokeOnCompletion { confirmationCause ->
						// A confirmation cancelled by the end of the session is not a failure.
						if (confirmationCause != null && confirmationCause !is CancellationException) {
							reportLateFailure(reportingRepository, confirmationCause)
						}
					}
				}
			}
			.single()
	}

	// Sends the outcome reports the server has not taken yet: the ones of earlier confirmations whose send failed.
	// The reports pending are the current session's: a report of an earlier session is dropped when it is staged.
	// Each intent sends the report staged for it when its turn comes, not the one the flush began with: a newer report
	// staged since is the one owed, and nothing is staged again here, so an older report cannot replace it.
	suspend fun flushPendingOutcomes() {
		val sessionMark = conversationRepository.sessionMark()

		outboxRepository.pendingOutcomes().forEach { owed ->
			val staged = outboxRepository.pendingOutcomes().firstOrNull { report -> report.intentId == owed.intentId }

			if (staged != null) sendReport(staged, sessionMark)
		}
	}

	// The proposed args must be of the kind. Edited args of that kind replace them; any other edited args are ignored.
	private fun <A : Any> resolveArgs(intent: Intent, editedArgs: IntentArgs?, narrow: (IntentArgs) -> A?): A {
		val proposed = narrow(intent.args)
			?: throw AssistantIntentIllegalArgumentException(AssistantIntentError.IntentKindMismatch)

		return editedArgs?.let(narrow) ?: proposed
	}

	private suspend fun <A : Any> executeAccepted(
		intent: Intent,
		args: A,
		execute: suspend (args: A, intent: Intent) -> IntentExecution,
		reportsOutcome: Boolean,
		sessionMark: Int
	): OutcomeKind {
		// The session may have ended while the server decided: what the intent asks for belongs to the account that left.
		// Nothing runs and nothing is written here, so the confirmation ends cancelled.
		if (!isCurrentSession(sessionMark)) return OutcomeKind.CANCELLED

		val execution = execute(args, intent)
		val report = IntentOutcomeReport(
			intentId = intent.id,
			outcome = execution.outcome,
			mutationIds = executedMutationIds(args, intent),
			detailCode = execution.detailCode,
			newRevision = execution.newRevision,
			itemOutcomes = execution.itemOutcomes
		).takeIf { reportsOutcome }

		if (report != null) sendOutcome(report, sessionMark)

		// The intent is marked before its queued mutations are followed: a settle that lands first would be overwritten by
		// the queued mark, and the intent would stay queued after its final outcome.
		conversationRepository.markIntentResolved(
			intent.id,
			execution.outcome,
			sessionMark,
			intent.resolvedCount(execution, selected = executedMutationIds(args, intent).size),
			detailCode = execution.detailCode
		)

		if (report != null && execution.outcome == OutcomeKind.QUEUED) followQueued(intent, report, sessionMark)

		return execution.outcome
	}

	// The mutations still queued: the ones a batch names as queued, or the single mutation of a queued intent. Once they
	// settle, the report that ends the intent is sent with their outcomes in place of the queued ones.
	private fun followQueued(intent: Intent, report: IntentOutcomeReport, sessionMark: Int) {
		val queued = report.itemOutcomes
			.filter { item -> item.outcome == OutcomeKind.QUEUED }
			.map { item -> item.mutationId }
			.ifEmpty { report.mutationIds }
		if (queued.isEmpty()) return

		queuedMutationsRepository.followQueued(queued, untilMillis = intent.expiresAtMillis) { settled ->
			settleQueued(intent, report, settled, sessionMark)
		}
	}

	// Runs when the queued mutations settle, which can be after the session of the intent has ended: then nothing is
	// written or sent, because the outcome belongs to the account that left.
	private suspend fun settleQueued(
		intent: Intent,
		report: IntentOutcomeReport,
		settled: List<MutationOutcome>,
		sessionMark: Int
	) {
		if (!isCurrentSession(sessionMark)) return

		val byId = settled.associateBy { item -> item.mutationId }
		val items = report.itemOutcomes.ifEmpty {
			report.mutationIds.map { mutationId -> MutationOutcome(mutationId, OutcomeKind.QUEUED) }
		}
		val execution = items.map { item -> byId[item.mutationId] ?: item }.toIntentExecution() ?: return

		sendOutcome(
			report.copy(
				outcome = execution.outcome,
				detailCode = execution.detailCode,
				newRevision = execution.newRevision ?: report.newRevision,
				itemOutcomes = execution.itemOutcomes
			),
			sessionMark
		)
		conversationRepository.markIntentResolved(
			intent.id,
			execution.outcome,
			sessionMark,
			intent.resolvedCount(execution, selected = report.mutationIds.size),
			detailCode = execution.detailCode
		)
	}

	private fun isCurrentSession(sessionMark: Int): Boolean = sessionMark == conversationRepository.sessionMark()

	// Nothing ran, so the report names no mutation.
	private suspend fun closeWithoutRunning(intent: Intent, outcome: OutcomeKind, sessionMark: Int): OutcomeKind {
		sendOutcome(IntentOutcomeReport(intentId = intent.id, outcome = outcome), sessionMark)
		conversationRepository.markIntentResolved(intent.id, outcome, sessionMark)

		return outcome
	}

	// The report stays staged until the server settles it: it takes the report, or refuses it for good, and then it
	// is no longer owed. A report that got no answer stays staged for the next flush, and the intent is resolved here
	// meanwhile.
	// A report of a session that has ended is neither kept nor sent: the request would carry the credentials of the
	// account that follows, and the report is for the one that left.
	private suspend fun sendOutcome(report: IntentOutcomeReport, sessionMark: Int) {
		if (!isCurrentSession(sessionMark)) return

		outboxRepository.stageOutcome(report, sessionMark)

		sendReport(report, sessionMark)
	}

	// Sends a staged report: the server takes it and it is cleared, or it stays staged for the next flush.
	private suspend fun sendReport(report: IntentOutcomeReport, sessionMark: Int) {
		if (!isCurrentSession(sessionMark)) return

		val failure = runCatching { intentRepository.reportOutcome(report) }.exceptionOrNull()

		if (failure == null) {
			outboxRepository.clearOutcome(report)
		} else if (failure is CancellationException) {
			throw failure
		}
	}
}

// The mutations the run asked for: the marked rows of a batch, the ones of other changing args, or the binding's.
private fun executedMutationIds(args: Any, intent: Intent): List<String> = when (args) {
	is AddEvaluationsArgs -> args.items.filter { item -> item.selected }.map { item -> item.mutationId }
	is MutatingIntentArgs -> args.mutationIds
	else -> intent.binding.mutationIds
}

// The count of an intent that adds evaluations: the rows it marked, and the ones its run applied. A run that answered
// without a row of its own applied all the marked rows when it confirmed. Null for any other intent, and for an outcome
// that applied no row (a conflict, an expiry or a rejection): there is no count to keep.
private fun Intent.resolvedCount(execution: IntentExecution, selected: Int): IntentResolvedCount? {
	val countedOutcomes = setOf(OutcomeKind.CONFIRMED, OutcomeKind.QUEUED, OutcomeKind.FAILED)

	if (args !is AddEvaluationsArgs || execution.outcome !in countedOutcomes) return null

	val landed = execution.itemOutcomes.count { item -> item.outcome == OutcomeKind.CONFIRMED }
	val applied = when {
		execution.itemOutcomes.isNotEmpty() -> landed
		execution.outcome == OutcomeKind.CONFIRMED -> selected
		else -> 0
	}

	return IntentResolvedCount(applied = applied, selected = selected)
}

// A failure no caller waits for is reported the way a use case reports a handled failure: under the name of the
// confirmer, with the keys that tell the failure apart. A confirmation has no error to name, so the error key says so.
private fun reportLateFailure(reportingRepository: ReportingRepository, failure: Throwable) {
	val rootCause = failure.rootCause()

	reportingRepository.setCustomKey("use-case", AssistantIntentConfirmer::class.reportingName())
	reportingRepository.setCustomKey("is-handled", true)
	reportingRepository.setCustomKey("throwable-class", failure.reportingName())
	reportingRepository.setCustomKey("throwable-message", failure.reportingMessage())
	reportingRepository.setCustomKey("root-cause-class", rootCause.reportingName())
	reportingRepository.setCustomKey("root-cause-message", rootCause.reportingMessage())
	reportingRepository.setCustomKey("error-class", "none")
	reportingRepository.logException(failure)
}
