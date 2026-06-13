package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermEditSeed
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoadSyntheticTermEditSeedUseCaseTest {
	@Test
	fun execute_mapsSyntheticTermIntoEditSeed_withPeriodAndSubjects() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(id = "historical", periodYear = 2024),
					academicTerm(
						id = "syn-1",
						kind = TermKind.SYNTHETIC,
						periodYear = 2027,
						periodCode = AcademicTermPeriod.SEP_DEC,
						attempts = listOf(
							academicAttempt(
								subjectCode = "MA1112",
								outcome = AttemptOutcome.PENDING,
								id = "syn-1-MA1112",
								subjectName = "Matemáticas VI",
								credits = 4
							),
							academicAttempt(
								subjectCode = "EP1120",
								outcome = AttemptOutcome.PENDING,
								id = "syn-1-EP1120",
								subjectName = "Pasantía",
								credits = 3,
								gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL
							)
						)
					)
				)
			)
		)
		val useCase = createUseCase(repository)

		useCase.execute("syn-1").test {
			val seed = awaitLoadingThenData(this)

			assertEquals(
				SyntheticTermEditSeed(
					termId = "syn-1",
					termKey = "2027-SEP_DEC",
					period = SyntheticTermPeriodOption(
						periodYear = 2027,
						periodCode = AcademicTermPeriod.SEP_DEC
					),
					subjects = listOf(
						SyntheticTermSubject(
							attemptId = "syn-1-MA1112",
							subjectCode = "MA1112",
							name = "Matemáticas VI",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC
						),
						SyntheticTermSubject(
							attemptId = "syn-1-EP1120",
							subjectCode = "EP1120",
							name = "Pasantía",
							credits = 3,
							gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL
						)
					)
				),
				seed
			)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsUnmappedError_whenRequestedTermIsNotSynthetic() = runTest {
		val repository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(id = "historical", periodYear = 2024)
				)
			)
		)
		val useCase = createUseCase(repository)

		useCase.execute("historical").test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsUnmappedError_whenRecordIsUnavailable() = runTest {
		val repository = ControllableAcademicRecordRepository()
		repository.recordAvailable = false
		val useCase = createUseCase(repository)

		useCase.execute("syn-1").test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}
	}

	private fun createUseCase(
		repository: ControllableAcademicRecordRepository
	): LoadSyntheticTermEditSeedUseCase {
		return LoadSyntheticTermEditSeedUseCase(
			repository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)
	}
}
