package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetRecordViewModeUseCaseTest {
	@Test
	fun execute_persistsRequestedViewMode_andEmitsUnit() = runTest {
		val selectionRepository = RecordingRecordSelectionRepository(
			initialViewMode = RecordViewMode.Historical
		)
		val useCase = SetRecordViewModeUseCase(
			recordSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(RecordViewMode.Projection).test {
			assertEquals(Unit, awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(listOf(RecordViewMode.Projection), selectionRepository.setViewModeCalls)
		assertEquals(RecordViewMode.Projection, selectionRepository.getRecordViewMode())
	}

	@Test
	fun execute_recordsEveryViewModeChange_inOrder() = runTest {
		val selectionRepository = RecordingRecordSelectionRepository()
		val useCase = SetRecordViewModeUseCase(
			recordSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(RecordViewMode.Projection).test {
			awaitLoadingThenData(this)
			awaitComplete()
		}
		useCase.execute(RecordViewMode.Historical).test {
			awaitLoadingThenData(this)
			awaitComplete()
		}

		assertEquals(
			listOf(RecordViewMode.Projection, RecordViewMode.Historical),
			selectionRepository.setViewModeCalls
		)
		assertEquals(RecordViewMode.Historical, selectionRepository.getRecordViewMode())
	}
}
