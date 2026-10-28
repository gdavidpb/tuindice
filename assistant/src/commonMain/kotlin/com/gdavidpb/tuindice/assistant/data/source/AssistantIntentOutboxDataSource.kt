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

	// The report is cleared only when it is still the one staged for its intent, in one write. A newer report staged
	// meanwhile is still owed.
	override fun clearOutcome(report: IntentOutcomeReport) {
		conversation.updateConversation { current ->
			if (current.outcomesPending[report.intentId] == report) {
				current.copy(outcomesPending = current.outcomesPending - report.intentId)
			} else {
				current
			}
		}
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> {
		return conversation.observeConversation().value.outcomesPending.values.toList()
	}

	// The claims are the intents' runs in flight, kept in the conversation with the reports. The check that the intent is
	// still open is made in the same write as the claim, so a confirmation that read the intent open before it was
	// settled cannot claim it after. The transform may run again when the write races another one: the last run is the
	// one written, so its answer is the answer of the claim.
	override fun claimConfirmation(intentId: String, sessionMark: Int): Boolean {
		var claimed = false

		conversation.updateConversation { current ->
			claimed = current.sessionGeneration == sessionMark &&
				intentId in current.openIntents &&
				intentId !in current.confirming

			if (claimed) current.copy(confirming = current.confirming + intentId) else current
		}

		return claimed
	}

	// A session that ended took its claims with its conversation, so a release made for it changes nothing.
	override fun releaseConfirmation(intentId: String, sessionMark: Int) {
		conversation.updateConversation { current ->
			if (current.sessionGeneration != sessionMark) {
				current
			} else {
				current.copy(confirming = current.confirming - intentId)
			}
		}
	}
}
