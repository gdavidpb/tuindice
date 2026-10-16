package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.testing.ControllablePensumProgressRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableScheduleClockRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubDate
import com.gdavidpb.tuindice.summary.testing.hubEvaluation
import com.gdavidpb.tuindice.summary.testing.hubMeeting
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ObserveSummaryHubUseCaseTest {
	private val progress = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)
	private val expectedCareer = SummaryCreditsProgress(approvedCredits = 156, totalCredits = 240)

	// A term that is over, with a subject passed, and the term in progress of the same record.
	private val recordWithAClosedTerm = hubRecord(
		hubClosedTerm(year = 2025, attempts = listOf(hubPassedAttempt(code = "MA1111")), index = 13.0),
		hubTerm(
			attempts = listOf(
				hubAttempt(code = "CI5437", schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4)))
			)
		)
	)

	private val record = hubRecord(
		hubTerm(
			attempts = listOf(
				hubAttempt(code = "CI5437", schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4)))
			)
		)
	)

	@Test
	fun theHub_putsTogetherWhatTheFiveSourcesSay() = runTest {
		val useCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(record),
			evaluations = ControllableVisibleEvaluationsRepository(
				initialEvaluations = listOf(hubEvaluation(id = "late", code = "CI5437", date = hubDate(8)))
			),
			pensumProgress = ControllablePensumProgressRepository(progress),
			clock = ControllableScheduleClockRepository(scheduleNow(minuteOfDay = 10 * 60))
		)

		useCase.execute(Unit).test {
			val hub = awaitLoadingThenData(this)

			assertTrue(hub.inProgressWeek().classMoment!!.isInProgress)
			assertEquals(1, hub.attention.size)
			assertEquals(3, hub.inProgressTerm().academicWeek)
			assertEquals(expectedCareer, hub.career)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun theHub_followsTheClock_whenAMinutePasses() = runTest {
		val clock = ControllableScheduleClockRepository(scheduleNow(minuteOfDay = 10 * 60))
		val useCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(record),
			clock = clock
		)

		useCase.execute(Unit).test {
			assertTrue(awaitLoadingThenData(this).inProgressWeek().classMoment!!.isInProgress)

			clock.nowFlow.value = scheduleNow(minuteOfDay = 14 * 60)

			assertEquals(false, expectData().inProgressWeek().classMoment!!.isInProgress)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aNewStudentWithoutRecord_isTheFirstTermCase_notAnError() = runTest {
		val useCase = createObserveSummaryHubUseCase(
			pensumProgress = ControllablePensumProgressRepository(progress),
			prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false, isNewStudentWithoutRecord = true)
		)

		useCase.execute(Unit).test {
			val hub = awaitLoadingThenData(this)

			assertEquals(
				SummaryHub(
					attention = emptyList(),
					week = SummaryWeek.Idle(hasTerms = false),
					term = SummaryTerm.Idle(hasTerms = false, finished = null),
					career = expectedCareer,
					statistics = null
				),
				hub.copy(statistics = null)
			)
			assertTrue(assertNotNull(hub.statistics).closed.terms.isEmpty())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun theHub_hasNoTermWhileTheRecordIsNotReady_andGainsItWhenItIs() = runTest {
		val prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false)
		val useCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(record),
			pensumProgress = ControllablePensumProgressRepository(progress),
			prerequisite = prerequisite
		)

		useCase.execute(Unit).test {
			assertEquals(SummaryHub.Empty.copy(career = expectedCareer), awaitLoadingThenData(this))

			prerequisite.stateFlow.value = prerequisite.stateFlow.value.copy(isReady = true)

			assertEquals(3, expectData().inProgressTerm().academicWeek)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aSourceThatFails_isReportedAndReadAsHavingNothing_withoutTakingTheHubDown() = runTest {
		val failure = IllegalStateException("hub-source")
		val reporting = RecordingReportingRepository()
		val useCase = ObserveSummaryHubUseCase(
			visibleAcademicRecordRepository = ControllableVisibleAcademicRecordRepository(record),
			visibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(failure = failure),
			pensumProgressRepository = ControllablePensumProgressRepository(failure = failure),
			recordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
			scheduleClockRepository = ControllableScheduleClockRepository(),
			reportingRepository = reporting
		)

		useCase.execute(Unit).test {
			val hub = awaitLoadingThenData(this)

			assertNull(hub.career)
			assertNull(hub.statistics?.pensumProgress)
			assertEquals(false, hub.statistics?.currentTerm?.hasSyncedEvaluations)
			assertTrue(hub.inProgressTerm().subjects.isEmpty())
			assertEquals(3, hub.inProgressTerm().academicWeek)
			cancelAndIgnoreRemainingEvents()
		}

		assertEquals<List<Throwable>>(listOf(failure, failure), reporting.loggedExceptions)
	}

	@Test
	fun aRecordThatFails_leavesOnlyTheCareer() = runTest {
		val failure = IllegalStateException("hub-record")
		val useCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(record, failure = failure),
			pensumProgress = ControllablePensumProgressRepository(progress)
		)

		useCase.execute(Unit).test {
			assertEquals(SummaryHub.Empty.copy(career = expectedCareer), awaitLoadingThenData(this))
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun theClockMovesTheDay_butTheClosedStatisticsAreNotReadAgain() = runTest {
		val clock = ControllableScheduleClockRepository(scheduleNow(minuteOfDay = 10 * 60))
		val useCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(recordWithAClosedTerm),
			clock = clock
		)

		useCase.execute(Unit).test {
			val first = assertNotNull(awaitLoadingThenData(this).statistics)

			clock.nowFlow.value = scheduleNow(minuteOfDay = 14 * 60)

			val second = assertNotNull(expectData().statistics)
			// The same instance: the closed terms were read from the record once, not again for the minute that passed.
			assertSame(first.closed, second.closed)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun theFinishedTerm_andTheLastClosedStatistic_areReadFromOneProjection_fromTheRecordTheUseCaseObserves() = runTest {
		val useCase = createObserveSummaryHubUseCase(
			record = ControllableVisibleAcademicRecordRepository(
				hubRecord(
					hubClosedTerm(year = 2024, attempts = listOf(hubPassedAttempt(code = "MA1111")), index = 13.5),
					hubClosedTerm(year = 2025, attempts = listOf(hubPassedAttempt(code = "CI5311")), index = 12.0)
				)
			)
		)

		useCase.execute(Unit).test {
			val hub = awaitLoadingThenData(this)
			val finished = assertNotNull(assertIs<SummaryTerm.Idle>(hub.term).finished)
			val last = assertNotNull(assertNotNull(hub.statistics).lastClosedTerm)

			assertEquals(last.periodYear, finished.periodYear)
			assertEquals(last.index ?: 0.0, finished.index)
			assertEquals(last.approvedCredits, finished.approvedCredits)
			cancelAndIgnoreRemainingEvents()
		}
	}

	private suspend fun ReceiveTurbine<UseCaseState<SummaryHub, Nothing>>.expectData(): SummaryHub {
		return assertIs<UseCaseState.Data<SummaryHub>>(awaitItem()).value
	}
}

// The week and the term of a hub whose record is ready and has a term in progress.
private fun SummaryHub.inProgressWeek(): SummaryWeek.InProgress = assertIs<SummaryWeek.InProgress>(week)

private fun SummaryHub.inProgressTerm(): SummaryTerm.InProgress = assertIs<SummaryTerm.InProgress>(term)
