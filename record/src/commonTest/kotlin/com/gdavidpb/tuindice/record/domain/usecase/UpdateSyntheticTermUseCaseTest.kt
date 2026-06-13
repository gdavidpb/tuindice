package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class UpdateSyntheticTermUseCaseTest {
	@Test
	fun execute_keepsTermIdentityAndAttemptIds_whenPeriodKeyIsUnchanged() = runTest {
		val repository = repositoryWithSyntheticTerm()
		val useCase = createUseCase(repository)

		useCase.execute(
			createParams(
				subjects = listOf(
					syntheticSubject(subjectCode = "MA1112", attemptId = "syn-1-MA1112"),
					syntheticSubject(subjectCode = "FS1111")
				)
			)
		).test {
			assertEquals("syn-1", awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(
			listOf(
				SyntheticTermUpdateCommand(
					targetTermId = "syn-1",
					targetTermKey = "9999-JAN_MAR",
					termId = "syn-1",
					periodYear = 9999,
					periodCode = AcademicTermPeriod.JAN_MAR,
					attempts = listOf(
						syntheticAttemptSeed(attemptId = "syn-1-MA1112", subjectCode = "MA1112"),
						syntheticAttemptSeed(attemptId = "syn-1-FS1111", subjectCode = "FS1111")
					)
				)
			),
			repository.updatedTerms
		)
	}

	@Test
	fun execute_rekeysTermAndAttempts_whenPeriodChanges() = runTest {
		val repository = repositoryWithSyntheticTerm()
		val useCase = createUseCase(repository)

		useCase.execute(
			createParams(
				period = SyntheticTermPeriodOption(
					periodYear = 9999,
					periodCode = AcademicTermPeriod.APR_JUL
				),
				subjects = listOf(
					syntheticSubject(subjectCode = "MA1112", attemptId = "syn-1-MA1112")
				)
			)
		).test {
			assertEquals("9999-APR_JUL", awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(
			listOf(
				SyntheticTermUpdateCommand(
					targetTermId = "syn-1",
					targetTermKey = "9999-JAN_MAR",
					termId = "9999-APR_JUL",
					periodYear = 9999,
					periodCode = AcademicTermPeriod.APR_JUL,
					attempts = listOf(
						syntheticAttemptSeed(
							attemptId = "9999-APR_JUL-MA1112",
							subjectCode = "MA1112"
						)
					)
				)
			),
			repository.updatedTerms
		)
	}

	@Test
	fun execute_emitsRecordUnavailable_whenRecordIsMissing() = runTest {
		val repository = repositoryWithSyntheticTerm()
		repository.recordAvailable = false
		val useCase = createUseCase(repository)

		useCase.execute(createParams()).test {
			val error = awaitLoadingThenError(this)
			val validation = assertIs<RecordUseCaseError.SyntheticTermValidation>(error.error)
			assertEquals(SyntheticTermValidationError.RECORD_UNAVAILABLE, validation.reason)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.updatedTerms)
	}

	@Test
	fun execute_emitsTermNotFound_whenEditingTermIsMissing() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(academicTerm(id = "historical", periodYear = 2024))
			)
		)
		val useCase = createUseCase(repository)

		useCase.execute(createParams()).test {
			val error = awaitLoadingThenError(this)
			val validation = assertIs<RecordUseCaseError.SyntheticTermValidation>(error.error)
			assertEquals(SyntheticTermValidationError.TERM_NOT_FOUND, validation.reason)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.updatedTerms)
	}

	@Test
	fun execute_rejectsDuplicateSubjects_beforeSubmittingMutation() = runTest {
		val repository = repositoryWithSyntheticTerm()
		val useCase = createUseCase(repository)

		useCase.execute(
			createParams(
				subjects = listOf(
					syntheticSubject(subjectCode = "MA1112"),
					syntheticSubject(subjectCode = " ma1112 ")
				)
			)
		).test {
			val error = awaitLoadingThenError(this)
			val validation = assertIs<RecordUseCaseError.SyntheticTermValidation>(error.error)
			assertEquals(SyntheticTermValidationError.DUPLICATE_SUBJECT, validation.reason)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.updatedTerms)
	}

	@Test
	fun execute_emitsUnmappedError_whenEditingTermIdIsMissingFromParams() = runTest {
		val repository = repositoryWithSyntheticTerm()
		val useCase = createUseCase(repository)

		useCase.execute(
			createParams(editingTermId = null, editingTermKey = null)
		).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.updatedTerms)
	}

	private fun repositoryWithSyntheticTerm(): ControllableAcademicRecordRepository {
		return ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "syn-1",
						kind = TermKind.SYNTHETIC,
						periodYear = 9999,
						periodCode = AcademicTermPeriod.JAN_MAR,
						attempts = listOf(
							academicAttempt(
								subjectCode = "MA1112",
								outcome = AttemptOutcome.PENDING,
								id = "syn-1-MA1112"
							)
						)
					)
				)
			)
		)
	}

	private fun createUseCase(
		repository: ControllableAcademicRecordRepository
	): UpdateSyntheticTermUseCase {
		return UpdateSyntheticTermUseCase(
			repository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)
	}

	private fun createParams(
		editingTermId: String? = "syn-1",
		editingTermKey: String? = "9999-JAN_MAR",
		period: SyntheticTermPeriodOption = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		),
		subjects: List<SyntheticTermSubject> = listOf(syntheticSubject(subjectCode = "MA1112"))
	): CreateSyntheticTermParams {
		return CreateSyntheticTermParams(
			editingTermId = editingTermId,
			editingTermKey = editingTermKey,
			period = period,
			subjects = subjects
		)
	}

	private fun syntheticSubject(
		subjectCode: String,
		attemptId: String? = null
	): SyntheticTermSubject {
		return SyntheticTermSubject(
			attemptId = attemptId,
			subjectCode = subjectCode,
			name = subjectCode,
			credits = 4
		)
	}

	private fun syntheticAttemptSeed(
		attemptId: String,
		subjectCode: String
	): SyntheticTermUpdateCommand.SyntheticAttemptSeed {
		return SyntheticTermUpdateCommand.SyntheticAttemptSeed(
			attemptId = attemptId,
			subjectCode = subjectCode,
			subjectName = subjectCode,
			credits = 4,
			gradingMode = AttemptGradingMode.NUMERIC,
			score = AttemptScore.empty(),
			outcome = AttemptOutcome.PENDING
		)
	}
}
