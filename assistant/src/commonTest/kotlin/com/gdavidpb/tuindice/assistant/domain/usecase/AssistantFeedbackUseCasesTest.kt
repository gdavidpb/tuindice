package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.usecase.error.ReportAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ReportAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.RateAssistantMessageParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ReportAssistantMessageParams
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.ReportAssistantMessageParamsValidator
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.testing.dataValues
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantFeedbackUseCasesTest {
	private val feedback = RecordingAssistantFeedbackRepository()
	private val reporting = RecordingReportingRepository()

	private fun reportUseCase() = ReportAssistantMessageUseCase(
		feedbackRepository = feedback,
		reportingRepository = reporting,
		paramsValidator = ReportAssistantMessageParamsValidator(),
		exceptionHandler = ReportAssistantMessageExceptionHandler()
	)

	@Test
	fun aRatingIsSentForItsMessage() = runTest {
		RateAssistantMessageUseCase(feedback, reporting)
			.execute(RateAssistantMessageParams(messageId = "m1", useful = false))
			.dataValues()

		assertEquals(listOf(MessageFeedback(messageId = "m1", useful = false)), feedback.rated)
	}

	@Test
	fun aReportSendsItsReasonAndItsTrimmedComment() = runTest {
		reportUseCase()
			.execute(
				ReportAssistantMessageParams(
					messageId = "m1",
					reason = ReportReason.INCORRECT,
					comment = "  El dato no es así  "
				)
			)
			.dataValues()

		assertEquals(
			listOf(MessageReport(messageId = "m1", reason = ReportReason.INCORRECT, comment = "El dato no es así")),
			feedback.reported
		)
	}

	@Test
	fun aBlankCommentIsSentAsNoComment() = runTest {
		reportUseCase()
			.execute(ReportAssistantMessageParams(messageId = "m1", reason = ReportReason.OTHER, comment = "   "))
			.dataValues()

		assertEquals(
			listOf(MessageReport(messageId = "m1", reason = ReportReason.OTHER, comment = null)),
			feedback.reported
		)
	}

	@Test
	fun aCommentOverTheLimitIsRefusedWithATypedError() = runTest {
		val state = reportUseCase()
			.execute(
				ReportAssistantMessageParams(
					messageId = "m1",
					reason = ReportReason.HARMFUL,
					comment = "a".repeat(501)
				)
			)
			.lastState()

		assertEquals(UseCaseState.Error(ReportAssistantMessageError.CommentTooLong), state)
		assertTrue(feedback.reported.isEmpty())
	}

	@Test
	fun aCommentOfExactlyTheLimitIsSent() = runTest {
		reportUseCase()
			.execute(
				ReportAssistantMessageParams(
					messageId = "m1",
					reason = ReportReason.OTHER,
					comment = "a".repeat(500)
				)
			)
			.dataValues()

		assertEquals(1, feedback.reported.size)
	}
}
