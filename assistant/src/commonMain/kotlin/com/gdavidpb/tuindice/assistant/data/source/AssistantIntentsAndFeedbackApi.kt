package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import io.ktor.client.request.post

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
