package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import io.ktor.client.request.get
import io.ktor.client.request.post

internal suspend fun KtorAssistantApiDataSource.getStatus(): AssistantStatus {
	return send { get(assistantPath("status")) }.decodeBody()
}

internal suspend fun KtorAssistantApiDataSource.getStanding(): AbuseStanding {
	return send { get(assistantPath("standing")) }.decodeBody()
}

internal suspend fun KtorAssistantApiDataSource.postAbuseAppeal(appeal: AbuseAppeal) {
	send { post(assistantPath("abuse", "appeals")) { setJsonBody(appeal) } }
}
