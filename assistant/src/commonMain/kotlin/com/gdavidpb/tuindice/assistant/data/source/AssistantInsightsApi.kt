package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.insights.TermInsights
import io.ktor.client.request.get

internal suspend fun KtorAssistantApiDataSource.getInsights(): TermInsights {
	return send { get(assistantPath("insights")) }.decodeBody()
}
