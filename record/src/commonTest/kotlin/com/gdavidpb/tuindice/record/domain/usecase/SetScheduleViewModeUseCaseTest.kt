package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.testing.RecordingScheduleSelectionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetScheduleViewModeUseCaseTest {
	@Test
	fun execute_persistsRequestedView_andEmitsUnit() = runTest {
		val repository = RecordingScheduleSelectionRepository()
		val useCase = SetScheduleViewModeUseCase(
			scheduleSelectionRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(ScheduleViewMode.Week).test {
			assertEquals(Unit, awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(listOf(ScheduleViewMode.Week), repository.setViewModeCalls)
	}
}
