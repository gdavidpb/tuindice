package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.mapper.toClientRequestId
import com.gdavidpb.tuindice.assistant.domain.mapper.toScreenContext
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ExtractEvaluationsFromPhotoExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ExtractEvaluationsFromPhotoParams
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionLimits
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.io.encoding.Base64
import kotlin.time.Clock

/**
 * Reads the evaluations of a photo of a plan, and writes the proposal into the conversation: an assistant message with
 * the intent it carries, if any. The photo is encoded to the size the service takes and is not kept anywhere.
 */
class ExtractEvaluationsFromPhotoUseCase(
	private val imageEncoderRepository: ImageEncoderRepository,
	private val extractionRepository: AssistantExtractionRepository,
	private val conversationRepository: AssistantConversationRepository,
	private val clock: Clock,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: ExtractEvaluationsFromPhotoExceptionHandler
) : FlowUseCase<ExtractEvaluationsFromPhotoParams, ExtractionResult, ExtractEvaluationsFromPhotoError>() {

	override suspend fun executeOnBackground(params: ExtractEvaluationsFromPhotoParams): Flow<ExtractionResult> = flow {
		// The session the photo is read in: its proposal is written into that session's conversation only.
		val sessionMark = conversationRepository.sessionMark()
		val image = imageEncoderRepository.encode(params.file, ENCODING_SPEC)
		val clientRequestId = image.content.toClientRequestId()

		val request = ExtractionRequest(
			clientRequestId = clientRequestId,
			mediaType = image.mimeType,
			imageBase64 = Base64.Default.encode(image.content),
			screenContext = params.screenContext?.toScreenContext(),
			conversationId = conversationRepository.currentConversation().conversationId
		)

		val result = extractionRepository.extract(request)

		conversationRepository.addAssistantMessage(
			message = AssistantMessage(
				id = clientRequestId,
				parts = listOfNotNull<MessagePart>(Text(result.message), result.intent?.let(::IntentPart)),
				createdAtMillis = clock.now().toEpochMilliseconds()
			),
			intent = result.intent,
			sessionMark = sessionMark
		)

		emit(result)
	}

	private companion object {
		// The service takes an image of at most its longest edge. At 3,5 MB the encoded image grows by a third in base64,
		// which stays under the 5 MB the service accepts.
		val ENCODING_SPEC = ImageEncodingSpec(
			maxDimensionPx = ExtractionLimits.MAX_IMAGE_EDGE_PX,
			maxBytes = 3_500_000,
			jpegQuality = 80
		)
	}
}
