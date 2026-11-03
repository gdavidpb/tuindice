package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentContent
import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.error.UploadAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.UploadAssistantAttachmentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.UploadAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantAttachmentRepository
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentAccepted
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UploadAssistantAttachmentUseCaseTest {
	private val attachments = RecordingAssistantAttachmentRepository()

	private fun useCase() = UploadAssistantAttachmentUseCase(
		attachmentRepository = attachments,
		reportingRepository = RecordingReportingRepository(),
		exceptionHandler = UploadAssistantAttachmentExceptionHandler()
	)

	private fun photo() = PreparedAssistantAttachment(
		name = "plan.jpg",
		content = AssistantAttachmentContent(
			mediaType = "image/jpeg",
			sha256 = ABC_SHA256,
			bytes = "abc".encodeToByteArray()
		),
		thumbnail = null
	)

	private suspend fun upload(): UseCaseState<*, *> {
		return useCase().execute(UploadAssistantAttachmentParams(attachment = photo())).lastState()
	}

	@Test
	fun theServiceGetsTheBytesTheirDigestAndTheNameOfTheFile() = runTest {
		upload()

		val (content, name) = attachments.uploads.single()

		assertContentEquals("abc".encodeToByteArray(), content.bytes)
		assertEquals("image/jpeg", content.mediaType)
		assertEquals(ABC_SHA256, content.sha256)
		assertEquals("plan.jpg", name)
	}

	@Test
	fun theAnswerOfTheServiceIsTheDataOfTheUpload() = runTest {
		assertEquals(UseCaseState.Data<AttachmentAccepted>(attachments.accepted), upload())
	}

	@Test
	fun eachRefusalCodeReadsAsItsTypedErrorAndAnUnknownOneAsOther() = runTest {
		val expected = listOf(
			"assistant_disabled" to UploadAssistantAttachmentError.AssistantDisabled(retryAfterSeconds = 120),
			"consent_required" to UploadAssistantAttachmentError.ConsentRequired,
			"quota_exhausted" to UploadAssistantAttachmentError.QuotaExhausted(retryAfterSeconds = 120),
			"suspended" to UploadAssistantAttachmentError.Restricted,
			"guided_only" to UploadAssistantAttachmentError.GuidedOnly,
			"attachment_too_large" to UploadAssistantAttachmentError.TooLarge,
			"attachment_unsupported" to UploadAssistantAttachmentError.Unsupported,
			"attachment_unreadable" to UploadAssistantAttachmentError.Other,
			"attachment_daily_limit" to UploadAssistantAttachmentError.DailyLimit(retryAfterSeconds = 120),
			"attachment_expired" to UploadAssistantAttachmentError.Other,
			"unavailable" to UploadAssistantAttachmentError.Other
		)

		expected.forEach { (code, error) ->
			attachments.failure = AssistantRequestRefusedException(code = code, retryAfterSeconds = 120, message = code)

			assertEquals(UseCaseState.Error(error), upload(), code)
		}
	}

	@Test
	fun aFailureThatIsNotARefusalIsAnUnnamedError() = runTest {
		attachments.failure = IllegalStateException("sin red")

		assertEquals(UseCaseState.Error<UploadAssistantAttachmentError>(null), upload())
	}

	private companion object {
		// The SHA-256 of "abc", the test vector of the standard.
		const val ABC_SHA256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
	}
}
