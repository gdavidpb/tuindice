package com.gdavidpb.tuindice.record.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveRecordActionProcessorContractTest {
	@Test
	fun process_keepsLoading_whenRecordHasNoVisibleTermsAndNeverSynced() = runTest {
		val processor = processor(
			record = AcademicRecord(id = "record"),
			hasSyncedRecord = false
		)

		processor.process(
			action = Record.Action.ObserveRecord,
			sideEffect = {}
		).test {
			assertEquals(Record.State.Loading, awaitItem()(Record.State.Idle))

			awaitComplete()
		}
	}

	@Test
	fun process_reducesToEmpty_whenRecordHasNoVisibleTermsAfterSync() = runTest {
		val processor = processor(
			record = AcademicRecord(id = "record"),
			hasSyncedRecord = true
		)

		processor.process(
			action = Record.Action.ObserveRecord,
			sideEffect = {}
		).test {
			assertEquals(Record.State.Empty, awaitItem()(Record.State.Idle))

			awaitComplete()
		}
	}

	@Test
	fun process_keepsFailed_whenRecordHasNoVisibleTermsAndNeverSynced() = runTest {
		val processor = processor(
			record = AcademicRecord(id = "record"),
			hasSyncedRecord = false
		)

		processor.process(
			action = Record.Action.ObserveRecord,
			sideEffect = {}
		).test {
			assertEquals(Record.State.Failed, awaitItem()(Record.State.Failed))

			awaitComplete()
		}
	}

	@Test
	fun process_reducesToContent_whenRecordHasVisibleTermsBeforeSyncStateUpdates() = runTest {
		val processor = processor(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					AcademicTerm(
						id = "term",
						startAtMillis = 1L,
						endAtMillis = 2L,
						kind = TermKind.OFFICIAL_HISTORICAL,
						attempts = listOf(
							AcademicAttempt(
								id = "attempt",
								subjectCode = "MAT101",
								subjectName = "Matematica",
								credits = 4
							)
						)
					)
				)
			),
			hasSyncedRecord = false
		)

		processor.process(
			action = Record.Action.ObserveRecord,
			sideEffect = {}
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(Record.State.Idle))
			assertEquals("term", content.selectedTermId)

			awaitComplete()
		}
	}

	@Test
	fun process_reducesToContent_whenOfficialModeHasCurrentTerm() = runTest {
		val processor = processor(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					AcademicTerm(
						id = "current-term",
						startAtMillis = 1L,
						endAtMillis = 2L,
						kind = TermKind.OFFICIAL_CURRENT,
						attempts = listOf(
							AcademicAttempt(
								id = "attempt",
								subjectCode = "MAT101",
								subjectName = "Matematica",
								credits = 4
							)
						)
					)
				)
			),
			hasSyncedRecord = true
		)

		processor.process(
			action = Record.Action.ObserveRecord,
			sideEffect = {}
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(Record.State.Idle))
			assertEquals("current-term", content.selectedTermId)

			awaitComplete()
		}
	}

	private fun processor(
		record: AcademicRecord,
		hasSyncedRecord: Boolean
	): ObserveRecordActionProcessor {
		return ObserveRecordActionProcessor(
			observeRecordUseCase = ObserveRecordUseCase(
				academicRecordRepository = StubAcademicRecordRepository(
					record = record,
					hasSyncedRecord = hasSyncedRecord
				),
				recordSelectionRepository = StubRecordSelectionRepository(),
				reportingRepository = RecordingReportingRepository()
			)
		)
	}
}

private class StubAcademicRecordRepository(
	private val record: AcademicRecord,
	private val hasSyncedRecord: Boolean
) : AcademicRecordRepository {
	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = flowOf(record)

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(hasSyncedRecord)

	override suspend fun getAcademicRecord(): AcademicRecord? = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private class StubRecordSelectionRepository : RecordSelectionRepository {
	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> = flowOf(null)

	override fun observeRecordViewMode(): Flow<RecordViewMode> = flowOf(RecordViewMode.Official)

	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? = null

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) = Unit

	override suspend fun getRecordViewMode(): RecordViewMode = RecordViewMode.Official

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) = Unit
}
