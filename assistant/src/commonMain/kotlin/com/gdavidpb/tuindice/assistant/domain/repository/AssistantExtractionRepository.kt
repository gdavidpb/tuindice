package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult

/** Reads the evaluations of a photo of a plan. The service answers with a proposal and keeps no image. */
interface AssistantExtractionRepository {
	suspend fun extract(request: ExtractionRequest): ExtractionResult
}
