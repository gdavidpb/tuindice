package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantApiException
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import io.ktor.client.request.post

// The server refuses a decision on an intent that expired or conflicted. Those are answers the domain reads as results.
internal suspend fun KtorAssistantApiDataSource.decideIntent(decision: IntentDecision): IntentDecisionResult {
	return try {
		postIntentDecision(decision.intentId, decision)

		IntentDecisionResult.Accepted
	} catch (cause: AssistantApiException) {
		when (cause.code) {
			AssistantErrorCodes.INTENT_EXPIRED -> IntentDecisionResult.Expired
			AssistantErrorCodes.INTENT_CONFLICT -> IntentDecisionResult.Conflict
			else -> throw cause
		}
	}
}

internal suspend fun KtorAssistantApiDataSource.postIntentDecision(intentId: String, decision: IntentDecision) {
	send { post(assistantPath("intents", intentId, "decision")) { setJsonBody(decision) } }
}

internal suspend fun KtorAssistantApiDataSource.postIntentOutcome(intentId: String, report: IntentOutcomeReport) {
	send { post(assistantPath("intents", intentId, "outcome")) { setJsonBody(report) } }
}

internal suspend fun KtorAssistantApiDataSource.postFeedback(feedback: MessageFeedback) {
	send { post(assistantPath("feedback")) { setJsonBody(feedback) } }
}

internal suspend fun KtorAssistantApiDataSource.postReport(report: MessageReport) {
	send { post(assistantPath("reports")) { setJsonBody(report) } }
}
