package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport

interface AssistantIntentRepository {
	/**
	 * Sends the decision. The server may answer that the intent expired or conflicted: a result, not a failure.
	 */
	suspend fun decide(decision: IntentDecision): IntentDecisionResult

	/**
	 * Sends the outcome of an intent. It returns when the server has taken the report or refused it for good; it
	 * throws when the report got no answer that settles it, and it must be sent again.
	 */
	suspend fun reportOutcome(report: IntentOutcomeReport)
}
