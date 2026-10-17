package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport

interface AssistantIntentRepository {
	/**
	 * Sends the decision. The server may answer that the intent expired or conflicted: a result, not a failure.
	 */
	suspend fun decide(decision: IntentDecision): IntentDecisionResult

	suspend fun reportOutcome(report: IntentOutcomeReport)
}
