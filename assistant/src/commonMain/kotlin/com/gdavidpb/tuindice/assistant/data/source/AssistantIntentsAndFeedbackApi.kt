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

// The server settles an outcome report when it takes it (204), when it repeats the result it has, or when it refuses
// it for good: a 409 for an intent that expired or was resolved otherwise, or any other client error. A server error
// or a rate limit is no answer: it is raised, and so is a failure that never reached the server. The report is sent
// again then.
internal suspend fun KtorAssistantApiDataSource.reportIntentOutcome(report: IntentOutcomeReport) {
	try {
		postIntentOutcome(report.intentId, report)
	} catch (cause: AssistantApiException) {
		if (cause.httpStatus == TOO_MANY_REQUESTS_STATUS || cause.httpStatus >= SERVER_ERROR_STATUS) throw cause
	}
}

private const val TOO_MANY_REQUESTS_STATUS = 429
private const val SERVER_ERROR_STATUS = 500

internal suspend fun KtorAssistantApiDataSource.postFeedback(feedback: MessageFeedback) {
	send { post(assistantPath("feedback")) { setJsonBody(feedback) } }
}

internal suspend fun KtorAssistantApiDataSource.postReport(report: MessageReport) {
	send { post(assistantPath("reports")) { setJsonBody(report) } }
}
