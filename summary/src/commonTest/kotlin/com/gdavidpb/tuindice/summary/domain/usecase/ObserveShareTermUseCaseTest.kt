package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.RecordingShareBackgroundRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveShareTermUseCaseTest {
	@Test
	fun when_theRecordHasClosedTerms_then_theSourceCarriesThemWithTheCareerAndTheBackgrounds() = runTest {
		val useCase = useCase(
			record = hubRecord(hubClosedTerm(year = 2024, attempts = listOf(hubPassedAttempt(code = "CI1")), index = 4.0)),
			user = DEFAULT_SUMMARY_USER.copy(careerName = CAREER),
			backgrounds = RecordingShareBackgroundRepository(preferredId = otherShareBackground.id)
		)

		val source = withTimeout(TIMEOUT_MILLIS) { useCase.executeOnBackground(Unit).first() }

		assertEquals(listOf(2024), source.closedTerms.map { term -> term.periodYear })
		assertEquals(CAREER, source.careerName)
		assertEquals(
			listOf(ShareBackground.included.id, otherShareBackground.id),
			source.backgrounds.map { background -> background.id }
		)
		assertEquals(otherShareBackground.id, source.preferredBackgroundId)
	}

	@Test
	fun when_theRecordHasNoClosedTerm_then_theSourceHasNoTermButStillHasTheBackgrounds() = runTest {
		val useCase = useCase(record = hubRecord())

		val source = withTimeout(TIMEOUT_MILLIS) { useCase.executeOnBackground(Unit).first() }

		assertTrue(source.closedTerms.isEmpty())
		assertEquals(2, source.backgrounds.size)
	}

	@Test
	fun when_theSheetIsObserved_then_noBackgroundIsAskedFromTheServer() = runTest {
		val backgrounds = RecordingShareBackgroundRepository()
		val useCase = useCase(record = hubRecord(), backgrounds = backgrounds)

		withTimeout(TIMEOUT_MILLIS) { useCase.executeOnBackground(Unit).first() }

		assertEquals(0, backgrounds.refreshes)
	}

	private fun useCase(
		record: com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord,
		user: User = DEFAULT_SUMMARY_USER,
		backgrounds: RecordingShareBackgroundRepository = RecordingShareBackgroundRepository()
	) = ObserveShareTermUseCase(
		observeSummaryHubUseCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(initialRecord = record)
		),
		userRepository = RecordingUserRepository(users = flowOf(user)),
		shareBackgroundRepository = backgrounds,
		shareBackgroundSelectionRepository = backgrounds,
		reportingRepository = RecordingReportingRepository()
	)

	private companion object {
		const val CAREER = "Ingeniería de Computación"
		const val TIMEOUT_MILLIS = 3_000L
	}
}
