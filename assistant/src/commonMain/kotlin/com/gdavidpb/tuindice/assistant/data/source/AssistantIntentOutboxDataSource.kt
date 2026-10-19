package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport

// The intents' outbox: its reports are kept in the conversation state, so this is a window over the conversation.
class AssistantIntentOutboxDataSource(
	private val conversation: AssistantConversationDataSource
) : AssistantIntentOutboxRepository {
	// A report staged for a session that has ended is not kept for the one that follows.
	override fun stageOutcome(report: IntentOutcomeReport, sessionMark: Int) {
		conversation.updateConversation { current ->
			if (current.sessionGeneration != sessionMark) {
				current
			} else {
				current.copy(outcomesPending = current.outcomesPending + (report.intentId to report))
			}
		}
	}

	override fun clearOutcome(intentId: String) {
		conversation.updateConversation { current -> current.copy(outcomesPending = current.outcomesPending - intentId) }
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> {
		return conversation.observeConversation().value.outcomesPending.values.toList()
	}
}
