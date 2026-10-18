package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.insights.InsightFeedbackRequest
import com.gdavidpb.tuindice.chatcontract.insights.TermInsights
import io.ktor.client.request.get
import io.ktor.client.request.post

internal suspend fun KtorAssistantApiDataSource.getInsights(): TermInsights {
	return send { get(assistantPath("insights")) }.decodeBody()
}

// The thumbs of a term report. The server answers 204 when it takes them, 404 for a report that is not the student's own
// ready one, and 401 without a token: those are raised, and the caller decides what the student sees.
internal suspend fun KtorAssistantApiDataSource.postInsightFeedback(insightId: String, useful: Boolean) {
	send {
		post(assistantPath("insights", insightId, "feedback")) {
			setJsonBody(InsightFeedbackRequest(useful = useful))
		}
	}
}
