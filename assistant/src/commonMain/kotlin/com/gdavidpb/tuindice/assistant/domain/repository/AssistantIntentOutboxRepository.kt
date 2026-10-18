package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport

/**
 * The outcome reports the server has not settled yet. They are kept with the conversation: a report the server did not
 * answer is sent again, until the server takes it or refuses it for good.
 */
interface AssistantIntentOutboxRepository {
	/** Keeps an outcome report until the server settles it. */
	fun stageOutcome(report: IntentOutcomeReport)

	/** The server settled the report: it is no longer owed. */
	fun clearOutcome(intentId: String)

	/** The outcome reports the server has not settled yet. */
	fun pendingOutcomes(): List<IntentOutcomeReport>
}
