package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.model.ObservedSchedule
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.ControllableScheduleClockRepository
import com.gdavidpb.tuindice.record.testing.RecordingScheduleSelectionRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveScheduleUseCaseTest {
	@Test
	fun execute_emitsTheCurrentTermWithItsMeetings_andTheStoredView() = runTest {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "old", kind = TermKind.HISTORICAL),
				academicTerm(
					id = "now",
					kind = TermKind.CURRENT,
					attempts = listOf(
						academicAttempt(subjectCode = "CI5311").copy(
							section = 1,
							schedule = listOf(
								AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116")
							)
						)
					)
				)
			)
		)

		val observed = observe(
			record = record,
			hasSynced = true,
			viewMode = ScheduleViewMode.Week
		)

		assertEquals("now", observed.currentTerm?.id)
		assertEquals(listOf("CI5311"), observed.currentTerm?.attempts?.map { it.subjectCode })
		assertEquals(ScheduleViewMode.Week, observed.viewMode)
		assertTrue(observed.hasSyncedRecord)
		assertEquals(ScheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60), observed.now)
	}

	@Test
	fun execute_emitsAgainWhenTheMinuteChanges() = runTest {
		val clockRepository = ControllableScheduleClockRepository()
		val useCase = ObserveScheduleUseCase(
			academicRecordRepository = ControllableAcademicRecordRepository(initialHasSynced = true),
			scheduleSelectionRepository = RecordingScheduleSelectionRepository(),
			scheduleClockRepository = clockRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(8 * 60, awaitLoadingThenData(this).now.minuteOfDay)

			clockRepository.nowFlow.value = ScheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60 + 1)

			val next = assertIs<UseCaseState.Data<ObservedSchedule>>(awaitItem())

			assertEquals(8 * 60 + 1, next.value.now.minuteOfDay)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_withoutACurrentTerm_emitsNoTerm() = runTest {
		val observed = observe(
			record = AcademicRecord(id = "record", terms = listOf(academicTerm(id = "old"))),
			hasSynced = false,
			viewMode = ScheduleViewMode.Table
		)

		assertNull(observed.currentTerm)
		assertFalse(observed.hasSyncedRecord)
	}

	private suspend fun observe(
		record: AcademicRecord,
		hasSynced: Boolean,
		viewMode: ScheduleViewMode
	): ObservedSchedule {
		val useCase = ObserveScheduleUseCase(
			academicRecordRepository = ControllableAcademicRecordRepository(
				initialRecord = record,
				initialHasSynced = hasSynced
			),
			scheduleSelectionRepository = RecordingScheduleSelectionRepository(initialViewMode = viewMode),
			scheduleClockRepository = ControllableScheduleClockRepository(),
			reportingRepository = RecordingReportingRepository()
		)
		var observed: ObservedSchedule? = null

		useCase.execute(Unit).test {
			observed = awaitLoadingThenData(this)

			cancelAndIgnoreRemainingEvents()
		}

		return requireNotNull(observed)
	}
}
