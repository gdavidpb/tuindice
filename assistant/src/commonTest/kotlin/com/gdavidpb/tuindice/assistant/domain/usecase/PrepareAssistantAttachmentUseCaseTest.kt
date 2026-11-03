package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.PrepareAssistantAttachmentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.PrepareAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.testing.RecordingImageEncoderRepository
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.exception.EncodedImageTooLargeException
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PrepareAssistantAttachmentUseCaseTest {
	private val encoder = RecordingImageEncoderRepository()

	private fun useCase() = PrepareAssistantAttachmentUseCase(
		imageEncoderRepository = encoder,
		reportingRepository = RecordingReportingRepository(),
		exceptionHandler = PrepareAssistantAttachmentExceptionHandler()
	)

	private suspend fun prepare(file: PlatformFile) =
		useCase().execute(PrepareAssistantAttachmentParams(file = file)).lastState()

	private suspend fun preparedOf(file: PlatformFile): PreparedAssistantAttachment {
		return (prepare(file) as UseCaseState.Data<*>).value as PreparedAssistantAttachment
	}

	@Test
	fun aPhotoIsEncodedToTheLimitsOfTheServiceAndToASmallThumbnail() = runTest {
		prepare(PlatformFile("plan.jpg"))

		assertEquals(
			listOf(
				ImageEncodingSpec(
					maxDimensionPx = AttachmentLimits.MAX_IMAGE_EDGE_PX,
					maxBytes = AttachmentLimits.MAX_IMAGE_BYTES,
					jpegQuality = 80
				),
				ImageEncodingSpec(maxDimensionPx = 256, maxBytes = Int.MAX_VALUE, jpegQuality = 70)
			),
			encoder.specs
		)
	}

	@Test
	fun aPhotoIsSentAsItsEncodedBytesWithTheirDigestAndItsThumbnail() = runTest {
		encoder.content = "abc".encodeToByteArray()

		val prepared = preparedOf(PlatformFile("plan.jpg"))

		assertEquals("plan.jpg", prepared.name)
		assertEquals("image/jpeg", prepared.content.mediaType)
		assertEquals(ABC_SHA256, prepared.content.sha256)
		assertEquals("abc", prepared.content.bytes.decodeToString())
		assertEquals("abc", prepared.thumbnail?.decodeToString())
	}

	@Test
	fun aPhotoStillOverTheLimitOnceEncodedIsRefusedAsTooLarge() = runTest {
		encoder.failure = EncodedImageTooLargeException(
			encodedBytes = AttachmentLimits.MAX_IMAGE_BYTES + 1,
			maxBytes = AttachmentLimits.MAX_IMAGE_BYTES
		)

		assertEquals(
			UseCaseState.Error(PrepareAssistantAttachmentError.TooLarge),
			prepare(PlatformFile("plan.jpg"))
		)

		// The thumbnail is not made for a photo that is refused.
		assertEquals(1, encoder.specs.size)
	}

	// A photo is sent as the JPEG the app encodes it to, so the name says jpg, whatever the file was called.
	@Test
	fun aPhotoOfAnotherTypeIsNamedAsTheJpegItIsSentAs() = runTest {
		assertEquals("IMG_2041.jpg", preparedOf(PlatformFile("IMG_2041.HEIC")).name)
		assertEquals("plan.JPEG", preparedOf(PlatformFile("plan.JPEG")).name)
		assertEquals("sin-extension.jpg", preparedOf(PlatformFile("sin-extension")).name)
	}

	// The encoder does not read a file that is not a photo: that read fails with no reason the chat can name, so the
	// attachment says it failed for another reason, and nothing is sent for it.
	@Test
	fun aFileTheEncoderCannotReadFailsTheReadWithNoNamedReason() = runTest {
		encoder.failure = IllegalArgumentException("Image content is not a supported image.")

		val state = prepare(PlatformFile("notas.jpg"))

		assertIs<UseCaseState.Error<PrepareAssistantAttachmentError>>(state)
		assertNull(state.error)
	}

	private companion object {
		// The SHA-256 of "abc", the test vector of the standard.
		const val ABC_SHA256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
	}
}
