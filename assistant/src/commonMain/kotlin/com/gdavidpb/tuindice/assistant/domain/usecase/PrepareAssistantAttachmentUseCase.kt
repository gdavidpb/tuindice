package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantAttachmentRejectedException
import com.gdavidpb.tuindice.assistant.domain.mapper.toAttachmentSha256
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentContent
import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.PrepareAssistantAttachmentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.PrepareAssistantAttachmentParams
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Reads a photo the student picked into the bytes the upload sends. The photo is encoded to the size the service takes
 * and gets a small thumbnail for the message. A file the encoder cannot read is not refused here: the encoder fails,
 * and the read fails with it.
 */
class PrepareAssistantAttachmentUseCase(
	private val imageEncoderRepository: ImageEncoderRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: PrepareAssistantAttachmentExceptionHandler
) : FlowUseCase<PrepareAssistantAttachmentParams, PreparedAssistantAttachment, PrepareAssistantAttachmentError>() {

	override suspend fun executeOnBackground(
		params: PrepareAssistantAttachmentParams
	): Flow<PreparedAssistantAttachment> = flow {
		emit(preparePhoto(params))
	}

	private suspend fun preparePhoto(params: PrepareAssistantAttachmentParams): PreparedAssistantAttachment {
		val image = imageEncoderRepository.encode(params.file, PHOTO_SPEC)

		if (image.content.size > AttachmentLimits.MAX_IMAGE_BYTES) {
			throw AssistantAttachmentRejectedException(PrepareAssistantAttachmentError.TooLarge)
		}

		val thumbnail = imageEncoderRepository.encode(params.file, THUMBNAIL_SPEC).content

		return PreparedAssistantAttachment(
			name = photoName(file = params.file, mediaType = image.mimeType),
			content = AssistantAttachmentContent(
				mediaType = image.mimeType,
				sha256 = image.content.toAttachmentSha256(),
				bytes = image.content
			),
			thumbnail = thumbnail
		)
	}

	// The name says the type the photo is sent as: a HEIC picked from the device goes as a JPEG, so its name ends in .jpg.
	private fun photoName(file: PlatformFile, mediaType: String): String {
		val name = file.name.ifBlank { PHOTO_NAME }
		val extension = name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
		val sentAsJpeg = mediaType == JPEG_MEDIA_TYPE && extension != "jpg" && extension != "jpeg"

		return if (sentAsJpeg) name.substringBeforeLast('.', missingDelimiterValue = name) + ".jpg" else name
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
		const val JPEG_MEDIA_TYPE = "image/jpeg"
	}
}
