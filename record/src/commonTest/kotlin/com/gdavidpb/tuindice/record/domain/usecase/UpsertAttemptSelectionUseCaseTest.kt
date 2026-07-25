package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpsertAttemptSelectionUseCaseTest {
	@Test
	fun when_commitReturnsToAcademicValue_then_itDeletesOverride() = runTest {
		val repository = FakeAcademicRecordRepository(
			record = academicRecordWithExistingOverride(
				academicScore = 4,
				overrideScore = 5
			)
		)
		val useCase = createUseCase(repository = repository)

		useCase.executeOnBackground(
			UpsertAttemptSelectionParams(
				attemptId = "attempt-1",
				grade = 4,
				outcome = null
			)
		).single()

		assertEquals(listOf("attempt-1"), repository.deletedAttemptIds)
		assertEquals(emptyList(), repository.upsertCalls)
	}

	private fun createUseCase(
		repository: FakeAcademicRecordRepository,
		reportingRepository: ReportingRepository = RecordingReportingRepository()
	): UpsertAttemptSelectionUseCase {
		return UpsertAttemptSelectionUseCase(
			academicRecordRepository = repository,
			reportingRepository = reportingRepository,
			exceptionHandler = RecordExceptionHandler()
		)
	}

	private fun academicRecordWithExistingOverride(
		academicScore: Int,
		overrideScore: Int
	): AcademicRecord {
		return AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = "attempt-1",
							subjectCode = "PB5611",
							subjectName = "Probabilidad",
							credits = 10,
							academicScore = AttemptScore.numeric(academicScore),
							academicOutcome = AttemptOutcome.APPROVED
						)
					)
				)
			),
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = "attempt-1",
					score = AttemptScore.numeric(overrideScore),
					outcome = AttemptOutcome.APPROVED,
					updatedAtMillis = 10L
				)
			)
		)
	}
}

private class FakeAcademicRecordRepository(
	private var record: AcademicRecord?
) : AcademicRecordRepository {
	data class UpsertCall(
		val attemptId: String,
		val score: Int?,
		val outcome: AttemptOutcome?
	)

	val upsertCalls = mutableListOf<UpsertCall>()
	val deletedAttemptIds = mutableListOf<String>()

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	) {
		upsertCalls += UpsertCall(
			attemptId = attemptId,
			score = score?.numericValue,
			outcome = outcome
		)
	}

	override suspend fun deleteAttemptOverride(attemptId: String) {
		deletedAttemptIds += attemptId
	}

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}
