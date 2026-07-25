package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.model.ObservedRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ObserveRecordUseCaseTest {
	@Test
	fun execute_selectsNewestVisibleTerm_andPersistsSelection_whenNothingIsSelected() = runTest {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "old", periodYear = 2023),
				academicTerm(id = "new", periodYear = 2024),
				academicTerm(id = "syn", kind = TermKind.SYNTHETIC, periodYear = 9999)
			)
		)
		val academicRecordRepository = ControllableAcademicRecordRepository(initialRecord = record)
		val selectionRepository = RecordingRecordSelectionRepository()
		val useCase = createUseCase(academicRecordRepository, selectionRepository)

		useCase.execute(Unit).test {
			val observed = awaitLoadingThenData(this)

			assertEquals(record, observed.record)
			assertEquals(RecordViewMode.Historical, observed.viewMode)
			assertEquals("new", observed.selectedTermId)
			assertEquals(false, observed.hasSyncedRecord)

			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(
			listOf(RecordViewMode.Historical to "new"),
			selectionRepository.setSelectedTermCalls
		)
	}

	@Test
	fun execute_reactsToRecordAndSyncEmissions_keepingStillVisibleSelection() = runTest {
		val initialRecord = AcademicRecord(
			id = "record",
			terms = listOf(academicTerm(id = "term-a", periodYear = 2024))
		)
		val academicRecordRepository = ControllableAcademicRecordRepository(initialRecord = initialRecord)
		val selectionRepository = RecordingRecordSelectionRepository()
		selectionRepository.setSelectedTermId(RecordViewMode.Historical, "term-a")
		selectionRepository.setSelectedTermCalls.clear()
		val useCase = createUseCase(academicRecordRepository, selectionRepository)

		useCase.execute(Unit).test {
			val initial = awaitLoadingThenData(this)
			assertEquals("term-a", initial.selectedTermId)
			assertEquals(false, initial.hasSyncedRecord)

			academicRecordRepository.hasSyncedFlow.value = true

			val synced = assertIs<UseCaseState.Data<ObservedRecord>>(awaitItem()).value
			assertEquals(true, synced.hasSyncedRecord)
			assertEquals("term-a", synced.selectedTermId)

			val updatedRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(id = "term-b", periodYear = 2025),
					academicTerm(id = "term-a", periodYear = 2024)
				)
			)
			academicRecordRepository.recordFlow.value = updatedRecord

			val refreshed = assertIs<UseCaseState.Data<ObservedRecord>>(awaitItem()).value
			assertEquals(updatedRecord, refreshed.record)
			assertEquals("term-a", refreshed.selectedTermId)
			assertEquals(true, refreshed.hasSyncedRecord)

			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(emptyList(), selectionRepository.setSelectedTermCalls)
	}

	@Test
	fun execute_doesNotClobberFreshSelection_whenRecordSnapshotHasNotCaughtUpYet() = runTest {
		val initialRecord = AcademicRecord(
			id = "record",
			terms = listOf(academicTerm(id = "term-a", periodYear = 2024))
		)
		val academicRecordRepository = ControllableAcademicRecordRepository(initialRecord = initialRecord)
		val selectionRepository = RecordingRecordSelectionRepository()
		selectionRepository.setSelectedTermId(RecordViewMode.Historical, "term-a")
		selectionRepository.setSelectedTermCalls.clear()
		val useCase = createUseCase(academicRecordRepository, selectionRepository)

		useCase.execute(Unit).test {
			val initial = awaitLoadingThenData(this)
			assertEquals("term-a", initial.selectedTermId)

			// Simulates SetSelectedTermUseCase explicitly selecting a term the record
			// snapshot doesn't know about yet (e.g. right after creating a synthetic
			// term: this settings-backed StateFlow dispatches immediately, while the
			// confirmed record snapshot is Room-backed and can lag behind it).
			selectionRepository.setSelectedTermId(RecordViewMode.Historical, "term-new")
			selectionRepository.setSelectedTermCalls.clear()

			val duringLag = assertIs<UseCaseState.Data<ObservedRecord>>(awaitItem()).value
			assertEquals("term-a", duringLag.selectedTermId)

			val updatedRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(id = "term-new", periodYear = 2025),
					academicTerm(id = "term-a", periodYear = 2024)
				)
			)
			academicRecordRepository.recordFlow.value = updatedRecord

			val caughtUp = assertIs<UseCaseState.Data<ObservedRecord>>(awaitItem()).value
			assertEquals("term-new", caughtUp.selectedTermId)

			cancelAndIgnoreRemainingEvents()
		}

		// The explicit selection must never have been overwritten back to "term-a"
		// while the record snapshot was catching up.
		assertEquals(emptyList(), selectionRepository.setSelectedTermCalls)
	}

	@Test
	fun execute_fallsBackToMirroredSelection_whenCurrentModeHasNoSelection() = runTest {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "old", periodYear = 2023),
				academicTerm(id = "syn", kind = TermKind.SYNTHETIC, periodYear = 9999)
			)
		)
		val academicRecordRepository = ControllableAcademicRecordRepository(initialRecord = record)
		val selectionRepository = RecordingRecordSelectionRepository(
			initialViewMode = RecordViewMode.Projection
		)
		selectionRepository.setSelectedTermId(RecordViewMode.Historical, "old")
		selectionRepository.setSelectedTermCalls.clear()
		val useCase = createUseCase(academicRecordRepository, selectionRepository)

		useCase.execute(Unit).test {
			val observed = awaitLoadingThenData(this)

			assertEquals(RecordViewMode.Projection, observed.viewMode)
			assertEquals("old", observed.selectedTermId)

			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(
			listOf(RecordViewMode.Projection to "old"),
			selectionRepository.setSelectedTermCalls
		)
	}

	@Test
	fun execute_emitsNullSelection_whenRecordHasNoTerms() = runTest {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(id = "record")
		)
		val selectionRepository = RecordingRecordSelectionRepository()
		val useCase = createUseCase(academicRecordRepository, selectionRepository)

		useCase.execute(Unit).test {
			val observed = awaitLoadingThenData(this)

			assertNull(observed.selectedTermId)
			assertEquals(RecordViewMode.Historical, observed.viewMode)

			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(emptyList(), selectionRepository.setSelectedTermCalls)
	}

	@Test
	fun execute_emitsNullSelection_whenNoTermIsVisibleInHistoricalMode() = runTest {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(id = "syn", kind = TermKind.SYNTHETIC, periodYear = 9999)
				)
			)
		)
		val selectionRepository = RecordingRecordSelectionRepository()
		val useCase = createUseCase(academicRecordRepository, selectionRepository)

		useCase.execute(Unit).test {
			val observed = awaitLoadingThenData(this)

			assertNull(observed.selectedTermId)

			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(emptyList(), selectionRepository.setSelectedTermCalls)
	}

	private fun createUseCase(
		academicRecordRepository: ControllableAcademicRecordRepository,
		selectionRepository: RecordingRecordSelectionRepository
	): ObserveRecordUseCase {
		return ObserveRecordUseCase(
			academicRecordRepository = academicRecordRepository,
			recordSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository()
		)
	}
}
