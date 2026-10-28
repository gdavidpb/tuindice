package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantAppealError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantAppealExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.AppealAssistantRestrictionParamsValidator
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConsentRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantStandingRepository
import com.gdavidpb.tuindice.assistant.testing.dataValues
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantStandingUseCasesTest {
	private val standing = RecordingAssistantStandingRepository()
	private val consent = RecordingAssistantConsentRepository()
	private val reporting = RecordingReportingRepository()

	private fun appealUseCase() = AppealAssistantRestrictionUseCase(
		standingRepository = standing,
		reportingRepository = reporting,
		paramsValidator = AppealAssistantRestrictionParamsValidator(),
		exceptionHandler = AssistantAppealExceptionHandler()
	)

	@Test
	fun theStatusOfTheAssistantIsReadFromTheServer() = runTest {
		val values = GetAssistantStatusUseCase(standing, reporting).execute(Unit).dataValues()

		assertEquals(listOf(standing.answer), values)
	}

	@Test
	fun anAppealIsSentTrimmed() = runTest {
		appealUseCase().execute("  Creo que es un error  ").dataValues()

		assertEquals(listOf(AbuseAppeal(text = "Creo que es un error")), standing.appeals)
	}

	@Test
	fun aBlankAppealIsEmptyAndOneOverTheLimitIsTooLong() = runTest {
		assertEquals(UseCaseState.Error(AssistantAppealError.Empty), appealUseCase().execute("  ").lastState())
		assertEquals(
			UseCaseState.Error(AssistantAppealError.TooLong),
			appealUseCase().execute("a".repeat(AbuseAppeal.MAX_LENGTH + 1)).lastState()
		)

		assertTrue(standing.appeals.isEmpty())
	}

	@Test
	fun anAppealOfExactlyTheLimitIsSent() = runTest {
		appealUseCase().execute("a".repeat(AbuseAppeal.MAX_LENGTH)).dataValues()

		assertEquals(1, standing.appeals.size)
	}

	@Test
	fun theConsentIsGrantedThroughTheConsentPortOfTheBase() = runTest {
		AcceptAssistantConsentUseCase(consent, testSessionCoroutineScope(), reporting).execute(true).dataValues()

		assertEquals(listOf(true), consent.changes)
	}

	// The caller leaves while the server answers the consent: the write belongs to the session, so it is not cut short.
	@Test
	fun aCallerThatLeavesWhileTheConsentIsWrittenDoesNotCutTheWriteShort() = runTest {
		val answer = CompletableDeferred<Unit>()
		consent.gate = answer
		val caller = launch {
			AcceptAssistantConsentUseCase(consent, testSessionCoroutineScope(), reporting).execute(true).collect {}
		}
		runCurrent()

		caller.cancel()
		answer.complete(Unit)
		runCurrent()

		assertEquals(listOf(true), consent.changes)
		assertTrue(consent.isAssistantConsentGranted())
	}
}
