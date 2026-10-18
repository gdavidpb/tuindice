package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantIntentIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.MutatingIntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock

/**
 * The order every confirmation follows, so the use cases that run an intent differ only in the port they call. The
 * intent must be open and of the kind the use case confirms. An intent past its deadline is reported as expired without
 * asking the server. The server decides: an expiry or a conflict it answers is reported without running anything. An
 * accepted intent runs on its port, and its outcome is reported with the mutations of its binding and marked resolved.
 * A decision whose answer the network lost may have been applied: its repetition is read as the decision made. An outcome
 * report the server does not take stays pending until a flush sends it again. Otherwise nothing here catches: a port
 * answers FAILED instead of throwing.
 */
class AssistantIntentConfirmer(
	private val conversationRepository: AssistantConversationRepository,
	private val intentRepository: AssistantIntentRepository,
	private val clock: Clock
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
		val intent = conversationRepository.currentConversation().openIntents[params.intentId]
			?: throw AssistantIntentIllegalArgumentException(AssistantIntentError.IntentNotOpen)

		val args = resolveArgs(intent, params.editedArgs, narrow)
		// Only edited args of the confirmed kind are sent: any other edit was ignored, so the server never sees it.
		val editedArgs = params.editedArgs?.takeIf { edited -> narrow(edited) != null }

		if (clock.now().toEpochMilliseconds() > intent.expiresAtMillis) {
			return closeWithoutRunning(intent, OutcomeKind.EXPIRED)
		}

		val decision = IntentDecision(intentId = intent.id, accepted = true, editedArgs = editedArgs)

		return when (decide(intent, decision)) {
			IntentDecisionResult.Accepted -> executeAccepted(intent, args, execute, reportsOutcome)
			IntentDecisionResult.Expired -> closeWithoutRunning(intent, OutcomeKind.EXPIRED)
			IntentDecisionResult.Conflict -> closeWithoutRunning(intent, OutcomeKind.CONFLICT)
		}
	}

	// Sends the outcome reports the server has not taken yet: the ones of earlier confirmations whose send failed.
	suspend fun flushPendingOutcomes() {
		conversationRepository.pendingOutcomes().forEach { report -> sendOutcome(report) }
	}

	// The decision as the server answers it. When the answer was lost to a failure of the network, the decision may have
	// been applied already: a Conflict to its repetition then is the decision as made, and the intent runs by its mutations,
	// which are idempotent.
	private suspend fun decide(intent: Intent, decision: IntentDecision): IntentDecisionResult {
		val repeated = conversationRepository.isDecisionUncertain(intent.id)
		val answer = runCatching { intentRepository.decide(decision) }

		answer.exceptionOrNull()?.let { failure ->
			if (failure is CancellationException) throw failure

			conversationRepository.markDecisionUncertain(intent.id)
			throw failure
		}

		conversationRepository.clearDecisionUncertain(intent.id)

		val result = answer.getOrThrow()

		return if (repeated && result == IntentDecisionResult.Conflict) IntentDecisionResult.Accepted else result
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
		reportsOutcome: Boolean
	): OutcomeKind {
		val execution = execute(args, intent)

		if (reportsOutcome) {
			sendOutcome(
				IntentOutcomeReport(
					intentId = intent.id,
					outcome = execution.outcome,
					mutationIds = executedMutationIds(args, intent),
					detailCode = execution.detailCode,
					newRevision = execution.newRevision
				)
			)
		}

		conversationRepository.markIntentResolved(intentId = intent.id, outcome = execution.outcome)

		return execution.outcome
	}

	// The mutations the run asked for: the marked rows of a batch, the ones of other changing args, or the binding's.
	private fun executedMutationIds(args: Any, intent: Intent): List<String> = when (args) {
		is AddEvaluationsArgs -> args.items.filter { item -> item.selected }.map { item -> item.mutationId }
		is MutatingIntentArgs -> args.mutationIds
		else -> intent.binding.mutationIds
	}

	// Nothing ran, so the report names no mutation.
	private suspend fun closeWithoutRunning(intent: Intent, outcome: OutcomeKind): OutcomeKind {
		sendOutcome(IntentOutcomeReport(intentId = intent.id, outcome = outcome))
		conversationRepository.markIntentResolved(intentId = intent.id, outcome = outcome)

		return outcome
	}

	// A report stays staged until the server takes it. Whether it was taken or not, the intent is resolved here, so the
	// conversation does not wait for the network; a staged report is the one the next flush sends.
	private suspend fun sendOutcome(report: IntentOutcomeReport) {
		conversationRepository.stageOutcome(report)

		val failure = runCatching { intentRepository.reportOutcome(report) }.exceptionOrNull()

		if (failure == null) {
			conversationRepository.clearOutcome(report.intentId)
		} else if (failure is CancellationException) {
			throw failure
		}
	}
}
