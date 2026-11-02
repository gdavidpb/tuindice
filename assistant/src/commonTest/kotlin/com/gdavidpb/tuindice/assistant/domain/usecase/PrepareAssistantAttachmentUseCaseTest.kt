package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.PrepareAssistantAttachmentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.PrepareAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantAttachmentFileRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingImageEncoderRepository
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrepareAssistantAttachmentUseCaseTest {
	private val encoder = RecordingImageEncoderRepository()
	private val files = RecordingAssistantAttachmentFileRepository()

	private fun useCase() = PrepareAssistantAttachmentUseCase(
		imageEncoderRepository = encoder,
		fileRepository = files,
		reportingRepository = RecordingReportingRepository(),
		exceptionHandler = PrepareAssistantAttachmentExceptionHandler()
	)

	private suspend fun prepare(file: PlatformFile, kind: AssistantAttachmentKind) =
		useCase().execute(PrepareAssistantAttachmentParams(file = file, kind = kind)).lastState()

	private suspend fun preparedOf(file: PlatformFile, kind: AssistantAttachmentKind): PreparedAssistantAttachment {
		return (prepare(file, kind) as UseCaseState.Data<*>).value as PreparedAssistantAttachment
	}

	@Test
	fun aPhotoIsEncodedToTheLimitsOfTheServiceAndToASmallThumbnail() = runTest {
		prepare(PlatformFile("plan.jpg"), AssistantAttachmentKind.PHOTO)

		assertEquals(
			listOf(
				ImageEncodingSpec(
					maxDimensionPx = AttachmentLimits.MAX_IMAGE_EDGE_PX,
					maxBytes = AttachmentLimits.MAX_IMAGE_BYTES,
					jpegQuality = 80
				),
				ImageEncodingSpec(maxDimensionPx = 256, maxBytes = 60_000, jpegQuality = 70)
			),
			encoder.specs
		)
	}

	@Test
	fun aPhotoIsSentAsItsEncodedBytesWithTheirDigestAndItsThumbnail() = runTest {
		encoder.content = "abc".encodeToByteArray()

		val prepared = preparedOf(PlatformFile("plan.jpg"), AssistantAttachmentKind.PHOTO)

		assertEquals(AssistantAttachmentKind.PHOTO, prepared.kind)
		assertEquals("plan.jpg", prepared.name)
		assertEquals("image/jpeg", prepared.content.mediaType)
		assertEquals(ABC_SHA256, prepared.content.sha256)
		assertContentEquals("abc".encodeToByteArray(), prepared.content.bytes)
		assertContentEquals("abc".encodeToByteArray(), prepared.thumbnail)
	}

	@Test
	fun aPhotoStillOverTheLimitOnceEncodedIsRefusedAsTooLarge() = runTest {
		encoder.content = ByteArray(AttachmentLimits.MAX_IMAGE_BYTES + 1)

		assertEquals(
			UseCaseState.Error(PrepareAssistantAttachmentError.TooLarge),
			prepare(PlatformFile("plan.jpg"), AssistantAttachmentKind.PHOTO)
		)
	}

	// A photo is sent as the JPEG the app encodes it to, so the name says jpg, whatever the file was called.
	@Test
	fun aPhotoOfAnotherTypeIsNamedAsTheJpegItIsSentAs() = runTest {
		assertEquals("IMG_2041.jpg", preparedOf(PlatformFile("IMG_2041.HEIC"), AssistantAttachmentKind.PHOTO).name)
		assertEquals("plan.JPEG", preparedOf(PlatformFile("plan.JPEG"), AssistantAttachmentKind.PHOTO).name)
		assertEquals("sin-extension.jpg", preparedOf(PlatformFile("sin-extension"), AssistantAttachmentKind.PHOTO).name)
	}

	@Test
	fun aPdfIsSentAsItIsAndNoPhotoIsEncoded() = runTest {
		files.bytes = PDF_BYTES

		val prepared = preparedOf(PlatformFile("programa.pdf"), AssistantAttachmentKind.PDF)

		assertEquals(AssistantAttachmentKind.PDF, prepared.kind)
		assertEquals("programa.pdf", prepared.name)
		assertEquals("application/pdf", prepared.content.mediaType)
		assertContentEquals(PDF_BYTES, prepared.content.bytes)
		assertNull(prepared.thumbnail)
		assertTrue(encoder.specs.isEmpty())
	}

	@Test
	fun aPdfOfExactlyTheLimitIsKeptAndOneByteMoreIsRefusedAsTooLarge() = runTest {
		files.bytes = PDF_HEADER + ByteArray(AttachmentLimits.MAX_PDF_BYTES - PDF_HEADER.size)

		assertEquals(
			AssistantAttachmentKind.PDF,
			preparedOf(PlatformFile("programa.pdf"), AssistantAttachmentKind.PDF).kind
		)

		files.bytes = PDF_HEADER + ByteArray(AttachmentLimits.MAX_PDF_BYTES - PDF_HEADER.size + 1)

		assertEquals(
			UseCaseState.Error(PrepareAssistantAttachmentError.TooLarge),
			prepare(PlatformFile("programa.pdf"), AssistantAttachmentKind.PDF)
		)
	}

	@Test
	fun aFileWithoutThePdfHeaderIsRefusedAsUnsupportedAndAnEmptyOneToo() = runTest {
		files.bytes = "Hola, esto es texto".encodeToByteArray()

		assertEquals(
			UseCaseState.Error(PrepareAssistantAttachmentError.Unsupported),
			prepare(PlatformFile("notas.pdf"), AssistantAttachmentKind.PDF)
		)

		files.bytes = byteArrayOf()

		assertEquals(
			UseCaseState.Error(PrepareAssistantAttachmentError.Unsupported),
			prepare(PlatformFile("notas.pdf"), AssistantAttachmentKind.PDF)
		)
	}

	private companion object {
		val PDF_HEADER = "%PDF-".encodeToByteArray()
		val PDF_BYTES = "%PDF-1.7\n%prueba\n".encodeToByteArray()

		// The SHA-256 of "abc", the test vector of the standard.
		const val ABC_SHA256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
	}
}
