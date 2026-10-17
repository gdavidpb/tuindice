package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import io.ktor.client.request.post

internal suspend fun KtorAssistantApiDataSource.postExtraction(request: ExtractionRequest): ExtractionResult {
	return send { post(assistantPath("extractions")) { setJsonBody(request) } }.decodeBody()
}
