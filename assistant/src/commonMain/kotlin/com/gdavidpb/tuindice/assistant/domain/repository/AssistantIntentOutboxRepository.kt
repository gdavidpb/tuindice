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

	/**
	 * The server settled the report: it is no longer owed. It is cleared only if it is still the report staged for its
	 * intent: a newer report staged meanwhile is still owed.
	 */
	fun clearOutcome(report: IntentOutcomeReport)

	/** The outcome reports the server has not settled yet. */
	fun pendingOutcomes(): List<IntentOutcomeReport>

	/**
	 * Claims the intent [intentId] for a confirmation that goes on, in the session [sessionMark] names. False when that
	 * session has ended, when the intent is no longer open, or when a claim of it is held already. The claim is held until
	 * [releaseConfirmation], or until the intent is marked resolved.
	 */
	fun claimConfirmation(intentId: String, sessionMark: Int): Boolean

	/** Releases the claim of the intent [intentId] made in the session [sessionMark] names. */
	fun releaseConfirmation(intentId: String, sessionMark: Int)
}
