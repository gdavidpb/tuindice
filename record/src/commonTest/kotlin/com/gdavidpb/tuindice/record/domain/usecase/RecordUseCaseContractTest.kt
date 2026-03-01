package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
import com.gdavidpb.tuindice.record.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordUseCaseContractTest {
	@Test
	fun getQuartersUseCase_emitsLoadingThenData() = runTest {
		val useCase = GetQuartersUseCase(
			quarterRepository = RecordingQuarterRepository(
				quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER))
			),
			exceptionHandler = GetQuartersExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(Unit).test {
			assertEquals(listOf(DEFAULT_RECORD_QUARTER), awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun setSubjectGradeUseCase_emitsLoadingThenData_andDelegatesSet() = runTest {
		val repository = RecordingQuarterRepository()
		val useCase = SetSubjectGradeUseCase(
			quarterRepository = repository,
			paramsValidator = SetSubjectGradeParamsValidator(),
			exceptionHandler = SetSubjectGradeExceptionHandler(
				reportingRepository = RecordingReportingRepository()
			)
		)
		val params = SetSubjectGradeParams(
			quarterId = DEFAULT_RECORD_QUARTER.id,
			subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
			grade = 70,
			commit = true
		)

		useCase.execute(params).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.setGradeCalls.size)
		assertEquals(true, repository.setGradeCalls.single().commit)
	}

	@Test
	fun setSubjectGradeUseCase_rejectsGradeOutsideRange() = runTest {
		val useCase = SetSubjectGradeUseCase(
			quarterRepository = RecordingQuarterRepository(),
			paramsValidator = SetSubjectGradeParamsValidator(),
			exceptionHandler = SetSubjectGradeExceptionHandler(
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(
			SetSubjectGradeParams(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = -1,
				commit = false
			)
		).test {
			val error = awaitLoadingThenError(this)
			assertEquals(SubjectUseCaseError.OutOfRangeGrade, error.error)
			awaitComplete()
		}
	}
}
