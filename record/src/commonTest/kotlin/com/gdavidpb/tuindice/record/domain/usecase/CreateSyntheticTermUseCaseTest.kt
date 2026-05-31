package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CreateSyntheticTermUseCaseTest {
	@Test
	fun execute_allowsFailedAndRetiredHistoricalSubjects() = runTest {
		val repository = RecordingCreateAcademicRecordRepository(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.FAILED),
							academicAttempt(subjectCode = "FS1111", outcome = AttemptOutcome.RETIRED)
						)
					)
				)
			)
		)
		val useCase = createUseCase(repository)

		useCase.execute(
			createParams(
				subjects = listOf(
					syntheticSubject("MA1112"),
					syntheticSubject("FS1111")
				)
			)
		).toList()

		assertEquals(
			listOf(listOf("MA1112", "FS1111")),
			repository.addedTerms.map { command ->
				command.attempts.map(SyntheticTermCreationCommand.SyntheticAttemptSeed::subjectCode)
			}
		)
	}

	@Test
	fun execute_rejectsApprovedHistoricalSubjectsBeforeSubmittingMutation() = runTest {
		val repository = RecordingCreateAcademicRecordRepository(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.APPROVED)
						)
					)
				)
			)
		)
		val states = createUseCase(repository)
			.execute(createParams(subjects = listOf(syntheticSubject("MA1112"))))
			.toList()

		val errorState = assertIs<UseCaseState.Error<String, RecordUseCaseError>>(states.last())
		val error = assertIs<RecordUseCaseError.SyntheticTermValidation>(errorState.error)
		assertEquals(SyntheticTermValidationError.SUBJECT_ALREADY_TAKEN, error.reason)
		assertEquals(emptyList(), repository.addedTerms)
	}

	@Test
	fun execute_rejectsExistingTermPeriodBeforeSubmittingMutation() = runTest {
		val repository = RecordingCreateAcademicRecordRepository(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "existing",
						kind = TermKind.SYNTHETIC,
						periodYear = 9999,
						periodCode = AcademicTermPeriod.JAN_MAR
					)
				)
			)
		)
		val states = createUseCase(repository)
			.execute(createParams(subjects = listOf(syntheticSubject("MA1112"))))
			.toList()

		val errorState = assertIs<UseCaseState.Error<String, RecordUseCaseError>>(states.last())
		val error = assertIs<RecordUseCaseError.SyntheticTermValidation>(errorState.error)
		assertEquals(SyntheticTermValidationError.TERM_ALREADY_EXISTS, error.reason)
		assertEquals(emptyList(), repository.addedTerms)
	}

	private fun createUseCase(repository: AcademicRecordRepository): CreateSyntheticTermUseCase {
		return CreateSyntheticTermUseCase(
			repository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)
	}
}

private class RecordingCreateAcademicRecordRepository(
	private val record: AcademicRecord?
) : AcademicRecordRepository {
	val addedTerms = mutableListOf<SyntheticTermCreationCommand>()

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = emptyFlow()

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

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) {
		addedTerms += command
	}

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private fun createParams(
	subjects: List<SyntheticTermSubject>,
	periodYear: Int = 9999,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.JAN_MAR
): CreateSyntheticTermParams {
	return CreateSyntheticTermParams(
		editingTermId = null,
		editingTermKey = null,
		period = SyntheticTermPeriodOption(
			periodYear = periodYear,
			periodCode = periodCode
		),
		subjects = subjects
	)
}

private fun academicTerm(
	id: String,
	kind: TermKind,
	periodYear: Int = 2024,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.JAN_MAR,
	attempts: List<AcademicAttempt> = emptyList()
): AcademicTerm {
	return AcademicTerm(
		id = id,
		periodYear = periodYear,
		periodCode = periodCode,
		kind = kind,
		attempts = attempts
	)
}

private fun academicAttempt(
	subjectCode: String,
	outcome: AttemptOutcome
): AcademicAttempt {
	return AcademicAttempt(
		id = "attempt-$subjectCode",
		subjectCode = subjectCode,
		subjectName = subjectCode,
		credits = 4,
		academicOutcome = outcome
	)
}

private fun syntheticSubject(subjectCode: String): SyntheticTermSubject {
	return SyntheticTermSubject(
		subjectCode = subjectCode,
		name = subjectCode,
		credits = 4
	)
}
