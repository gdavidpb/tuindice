package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantAttachmentRejectedException
import com.gdavidpb.tuindice.assistant.domain.mapper.toAttachmentSha256
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentContent
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantAttachmentFileRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.PrepareAssistantAttachmentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.PrepareAssistantAttachmentParams
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Reads a file the student picked into the bytes the upload sends. A photo is encoded to the size the service takes and
 * gets a small thumbnail for the message. A PDF is checked by its size and its header, so a file that is not a PDF is
 * refused here, before anything is uploaded.
 */
class PrepareAssistantAttachmentUseCase(
	private val imageEncoderRepository: ImageEncoderRepository,
	private val fileRepository: AssistantAttachmentFileRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: PrepareAssistantAttachmentExceptionHandler
) : FlowUseCase<PrepareAssistantAttachmentParams, PreparedAssistantAttachment, PrepareAssistantAttachmentError>() {

	override suspend fun executeOnBackground(
		params: PrepareAssistantAttachmentParams
	): Flow<PreparedAssistantAttachment> = flow {
		val prepared = when (params.kind) {
			AssistantAttachmentKind.PHOTO -> preparePhoto(params)
			AssistantAttachmentKind.PDF -> preparePdf(params)
		}

		emit(prepared)
	}

	private suspend fun preparePhoto(params: PrepareAssistantAttachmentParams): PreparedAssistantAttachment {
		val image = imageEncoderRepository.encode(params.file, PHOTO_SPEC)

		if (image.content.size > AttachmentLimits.MAX_IMAGE_BYTES) {
			throw AssistantAttachmentRejectedException(PrepareAssistantAttachmentError.TooLarge)
		}

		val thumbnail = imageEncoderRepository.encode(params.file, THUMBNAIL_SPEC).content

		return PreparedAssistantAttachment(
			kind = AssistantAttachmentKind.PHOTO,
			name = params.file.name.ifBlank { PHOTO_NAME },
			content = AssistantAttachmentContent(
				mediaType = image.mimeType,
				sha256 = image.content.toAttachmentSha256(),
				bytes = image.content
			),
			thumbnail = thumbnail
		)
	}

	private suspend fun preparePdf(params: PrepareAssistantAttachmentParams): PreparedAssistantAttachment {
		val bytes = fileRepository.readBytes(params.file)

		if (bytes.size > AttachmentLimits.MAX_PDF_BYTES) {
			throw AssistantAttachmentRejectedException(PrepareAssistantAttachmentError.TooLarge)
		}

		if (!bytes.startsWithPdfHeader()) {
			throw AssistantAttachmentRejectedException(PrepareAssistantAttachmentError.Unsupported)
		}

		return PreparedAssistantAttachment(
			kind = AssistantAttachmentKind.PDF,
			name = params.file.name.ifBlank { PDF_NAME },
			content = AssistantAttachmentContent(
				mediaType = AttachmentLimits.PDF_MEDIA_TYPE,
				sha256 = bytes.toAttachmentSha256(),
				bytes = bytes
			),
			thumbnail = null
		)
	}

	private fun ByteArray.startsWithPdfHeader(): Boolean {
		return size >= PDF_HEADER.size && PDF_HEADER.indices.all { index -> this[index] == PDF_HEADER[index] }
	}

	private companion object {
		// The photo is scaled to the longest edge the service takes, and encoded under its size limit.
		val PHOTO_SPEC = ImageEncodingSpec(
			maxDimensionPx = AttachmentLimits.MAX_IMAGE_EDGE_PX,
			maxBytes = AttachmentLimits.MAX_IMAGE_BYTES,
			jpegQuality = 80
		)

		// The thumbnail the message shows: small, so the conversation stays light.
		val THUMBNAIL_SPEC = ImageEncodingSpec(maxDimensionPx = 256, maxBytes = 60_000, jpegQuality = 70)

		const val PHOTO_NAME = "foto.jpg"
		const val PDF_NAME = "documento.pdf"
		val PDF_HEADER = "%PDF-".encodeToByteArray()
	}
}
