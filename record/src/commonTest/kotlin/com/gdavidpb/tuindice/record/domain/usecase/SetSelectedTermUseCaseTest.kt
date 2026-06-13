package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedTermParams
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SetSelectedTermUseCaseTest {
	@Test
	fun execute_persistsSelectionForRequestedViewMode_andEmitsUnit() = runTest {
		val selectionRepository = RecordingRecordSelectionRepository()
		val useCase = createUseCase(selectionRepository)

		useCase.execute(
			SetSelectedTermParams(
				viewMode = RecordViewMode.Projection,
				termId = "term-1"
			)
		).test {
			assertEquals(Unit, awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(
			listOf(RecordViewMode.Projection to "term-1"),
			selectionRepository.setSelectedTermCalls
		)
		assertEquals("term-1", selectionRepository.getSelectedTermId(RecordViewMode.Projection))
	}

	@Test
	fun execute_keepsSelectionsPerViewMode_isolated() = runTest {
		val selectionRepository = RecordingRecordSelectionRepository()
		val useCase = createUseCase(selectionRepository)

		useCase.execute(
			SetSelectedTermParams(
				viewMode = RecordViewMode.Historical,
				termId = "term-1"
			)
		).test {
			awaitLoadingThenData(this)
			awaitComplete()
		}

		assertEquals("term-1", selectionRepository.getSelectedTermId(RecordViewMode.Historical))
		assertNull(selectionRepository.getSelectedTermId(RecordViewMode.Projection))
	}

	private fun createUseCase(
		selectionRepository: RecordingRecordSelectionRepository
	): SetSelectedTermUseCase {
		return SetSelectedTermUseCase(
			recordSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository()
		)
	}
}
