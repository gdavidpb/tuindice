package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport

/**
 * The outcome reports the server has not settled yet. They are kept with the conversation: a report the server did not
 * answer is sent again, until the server takes it or refuses it for good.
 */
interface AssistantIntentOutboxRepository {
	/**
	 * Keeps an outcome report until the server settles it. [sessionMark] is the session the report belongs to, read before
	 * its intent's request: a report staged after that session ended is not kept for the one that follows.
	 */
	fun stageOutcome(report: IntentOutcomeReport, sessionMark: Int)

	/** The server settled the report: it is no longer owed. */
	fun clearOutcome(intentId: String)

	/** The outcome reports the server has not settled yet. */
	fun pendingOutcomes(): List<IntentOutcomeReport>
}
