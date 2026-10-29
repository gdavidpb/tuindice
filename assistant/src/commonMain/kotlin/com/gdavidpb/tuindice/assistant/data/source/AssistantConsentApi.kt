package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.consent.ConsentDecision
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import io.ktor.client.request.get
import io.ktor.client.request.put

internal suspend fun KtorAssistantApiDataSource.getConsent(): ConsentStatus {
	return send { get(assistantPath("consent")) }.decodeBody()
}

internal suspend fun KtorAssistantApiDataSource.putConsent(decision: ConsentDecision): ConsentStatus {
	return send { put(assistantPath("consent")) { setJsonBody(decision) } }.decodeBody()
}
