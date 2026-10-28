package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

/**
 * The user turns an intent down: the server is told, and the intent closes as the server answers it.
 * A decline the server takes closes the intent as cancelled; an answer that the intent expired, or is in
 * conflict, closes it as that. No outcome is reported.
 *
 * The decline claims its intent as a confirmation does. A confirmation that holds the intent is not declined: nothing
 * is sent or closed here, and the run settles the intent. The decline belongs to the session ([sessionCoroutineScope]),
 * so leaving the screen does not stop it: the decision is sent, the intent closes, and the claim is released.
 */
class DismissAssistantIntentUseCase(
	private val intentRepository: AssistantIntentRepository,
	private val conversationRepository: AssistantConversationRepository,
	override val reportingRepository: ReportingRepository,
	private val outboxRepository: AssistantIntentOutboxRepository,
	private val sessionCoroutineScope: SessionCoroutineScope
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val sessionMark = conversationRepository.sessionMark()

		// The decline is started in the session when the claim is taken. The claim ends with the decline, however the decline
		// ends. It is the decline's completion that releases it, not the completion of the flow: the flow ends when the
		// caller leaves, and the decline does not.
		val decline = if (outboxRepository.claimConfirmation(params, sessionMark)) {
			sessionCoroutineScope.async { declineAndClose(params, sessionMark) }.also { decline ->
				decline.invokeOnCompletion { outboxRepository.releaseConfirmation(params, sessionMark) }
			}
		} else {
			null
		}

		return flow {
			decline?.await()
			emit(Unit)
		}.onCompletion { cause ->
			// A caller that left before the decline ended does not await it, so a failure of the decline is reported here,
			// as the about module's writes are. A failure that reaches the caller is reported by FlowUseCase, and only once.
			if (cause is CancellationException) {
				decline?.invokeOnCompletion { declineCause ->
					// A decline cancelled by the end of the session is not a failure.
					if (declineCause != null && declineCause !is CancellationException) reportHandledException(declineCause)
				}
			}
		}
	}

	private suspend fun declineAndClose(params: String, sessionMark: Int) {
		val outcome = when (intentRepository.decide(IntentDecision(intentId = params, accepted = false))) {
			IntentDecisionResult.Accepted -> OutcomeKind.CANCELLED
			IntentDecisionResult.Expired -> OutcomeKind.EXPIRED
			IntentDecisionResult.Conflict -> OutcomeKind.CONFLICT
		}
		conversationRepository.markIntentResolved(intentId = params, outcome = outcome, sessionMark = sessionMark)
	}
}
