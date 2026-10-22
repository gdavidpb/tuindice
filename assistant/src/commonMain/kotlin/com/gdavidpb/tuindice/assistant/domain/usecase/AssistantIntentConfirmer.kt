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
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.MutatingIntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock

/**
 * The order every confirmation follows, so the use cases that run an intent differ only in the port they call. The
 * intent must be open and of the kind the use case confirms. An intent past its deadline is reported as expired without
 * asking the server. The server decides: an expiry or a conflict it answers is reported without running anything. An
 * accepted intent runs on its port, and its outcome is reported with the mutations of its binding and marked resolved.
 * A decision repeated after its answer was lost is answered as decided, so it runs; a conflict is final. An outcome
 * report stays in the outbox until the server settles it, and a flush sends it again when it was not answered. Nothing
 * here catches: a port answers FAILED instead of throwing.
 *
 * A report names how each mutation of a batch ended. An intent that answered QUEUED is not left there: its queued
 * mutations are followed in the app session, and once they settle (before the intent's deadline) the final outcome goes
 * through the outbox as one more report, and the intent is marked resolved with it.
 */
class AssistantIntentConfirmer(
	private val conversationRepository: AssistantConversationRepository,
	private val intentRepository: AssistantIntentRepository,
	private val clock: Clock,
	private val outboxRepository: AssistantIntentOutboxRepository,
	private val queuedMutationsRepository: AssistantQueuedMutationsRepository
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

		if (clock.now().toEpochMilliseconds() > intent.expiresAtMillis) {
			return closeWithoutRunning(intent, OutcomeKind.EXPIRED, sessionMark)
		}

		val decision = IntentDecision(intentId = intent.id, accepted = true, editedArgs = editedArgs)

		return when (intentRepository.decide(decision)) {
			IntentDecisionResult.Accepted -> executeAccepted(intent, args, execute, reportsOutcome, sessionMark)
			IntentDecisionResult.Expired -> closeWithoutRunning(intent, OutcomeKind.EXPIRED, sessionMark)
			IntentDecisionResult.Conflict -> closeWithoutRunning(intent, OutcomeKind.CONFLICT, sessionMark)
		}
	}

	// Sends the outcome reports the server has not taken yet: the ones of earlier confirmations whose send failed.
	// The reports pending are the current session's: a report of an earlier session is dropped when it is staged.
	suspend fun flushPendingOutcomes() {
		val sessionMark = conversationRepository.sessionMark()

		outboxRepository.pendingOutcomes().forEach { report -> sendOutcome(report, sessionMark) }
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
		val execution = execute(args, intent)

		if (reportsOutcome) {
			val report = IntentOutcomeReport(
				intentId = intent.id,
				outcome = execution.outcome,
				mutationIds = executedMutationIds(args, intent),
				detailCode = execution.detailCode,
				newRevision = execution.newRevision,
				itemOutcomes = execution.itemOutcomes
			)

			sendOutcome(report, sessionMark)
			if (execution.outcome == OutcomeKind.QUEUED) followQueued(intent, report, sessionMark)
		}

		conversationRepository.markIntentResolved(
			intent.id,
			execution.outcome,
			sessionMark,
			intent.resolvedCount(execution, selected = executedMutationIds(args, intent).size)
		)

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
			intent.resolvedCount(execution, selected = report.mutationIds.size)
		)
	}

	// The mutations the run asked for: the marked rows of a batch, the ones of other changing args, or the binding's.
	private fun executedMutationIds(args: Any, intent: Intent): List<String> = when (args) {
		is AddEvaluationsArgs -> args.items.filter { item -> item.selected }.map { item -> item.mutationId }
		is MutatingIntentArgs -> args.mutationIds
		else -> intent.binding.mutationIds
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

		val failure = runCatching { intentRepository.reportOutcome(report) }.exceptionOrNull()

		if (failure == null) {
			outboxRepository.clearOutcome(report.intentId)
		} else if (failure is CancellationException) {
			throw failure
		}
	}
}

// The count of an intent that adds evaluations: the rows it marked, and the ones its run applied. A run that answered
// without a row of its own applied all the marked rows when it confirmed. Null for any other intent.
private fun Intent.resolvedCount(execution: IntentExecution, selected: Int): IntentResolvedCount? {
	if (args !is AddEvaluationsArgs) return null

	val landed = execution.itemOutcomes.count { item -> item.outcome == OutcomeKind.CONFIRMED }
	val applied = when {
		execution.itemOutcomes.isNotEmpty() -> landed
		execution.outcome == OutcomeKind.CONFIRMED -> selected
		else -> 0
	}

	return IntentResolvedCount(applied = applied, selected = selected)
}
