package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.mapper.toScreenContext
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ExtractEvaluationsFromPhotoExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ExtractEvaluationsFromPhotoParams
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionLimits
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.Text
import io.ktor.util.sha256
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Reads the evaluations of a photo of a plan, and writes the proposal into the conversation: an assistant message with
 * the intent it carries, if any. The photo is encoded to the size the service takes and is not kept anywhere.
 */
class ExtractEvaluationsFromPhotoUseCase(
	private val imageEncoderRepository: ImageEncoderRepository,
	private val extractionRepository: AssistantExtractionRepository,
	private val conversationRepository: AssistantConversationRepository,
	private val clock: Clock,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<ExtractEvaluationsFromPhotoParams, ExtractionResult, ExtractEvaluationsFromPhotoError>() {
	override val exceptionHandler: ExceptionHandler<ExtractEvaluationsFromPhotoError> =
		ExtractEvaluationsFromPhotoExceptionHandler()

	override suspend fun executeOnBackground(params: ExtractEvaluationsFromPhotoParams): Flow<ExtractionResult> = flow {
		val image = imageEncoderRepository.encode(params.file, ENCODING_SPEC)
		val clientRequestId = image.content.clientRequestId()

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
			intent = result.intent
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

// The same photo gives the same id, so a retry of it reaches the service with the id of the proposal it already has.
// The id is the first 128 bits of the SHA-256 of the encoded bytes, written as a version 8 UUID.
private fun ByteArray.clientRequestId(): String {
	val bytes = sha256(this).copyOf(UUID_BYTES)

	bytes[VERSION_INDEX] = ((bytes[VERSION_INDEX].toInt() and VERSION_MASK) or VERSION_8).toByte()
	bytes[VARIANT_INDEX] = ((bytes[VARIANT_INDEX].toInt() and VARIANT_MASK) or VARIANT_RFC_4122).toByte()

	return Uuid.fromByteArray(bytes).toString()
}

private const val UUID_BYTES = 16
private const val VERSION_INDEX = 6
private const val VARIANT_INDEX = 8
private const val VERSION_MASK = 0x0F
private const val VERSION_8 = 0x80
private const val VARIANT_MASK = 0x3F
private const val VARIANT_RFC_4122 = 0x80
