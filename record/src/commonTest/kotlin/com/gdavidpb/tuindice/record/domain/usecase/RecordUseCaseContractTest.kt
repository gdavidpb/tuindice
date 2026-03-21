package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
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
import kotlin.test.assertIs
import kotlin.test.fail

class RecordUseCaseContractTest {
	@Test
	fun observeQuartersUseCase_emitsLoadingThenData() = runTest {
		val useCase = ObserveQuartersUseCase(
			quarterRepository = RecordingQuarterRepository(
				quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER))
			)
		)

		useCase.execute(Unit).test {
			assertEquals(listOf(DEFAULT_RECORD_QUARTER), awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun updateQuartersUseCase_emitsLoadingThenData_andDelegatesRefresh() = runTest {
		val repository = RecordingQuarterRepository()
		val useCase = UpdateQuartersUseCase(
			quarterRepository = repository,
			exceptionHandler = UpdateQuartersExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.updateQuartersCalls.value)
	}

	@Test
	fun setSubjectGradeUseCase_emitsLoadingThenData_andDelegatesSet() = runTest {
		val repository = RecordingQuarterRepository()
		val reportingRepository = RecordingReportingRepository()
		val useCase = SetSubjectGradeUseCase(
			quarterRepository = repository,
			paramsValidator = SetSubjectGradeParamsValidator(),
			exceptionHandler = SetSubjectGradeExceptionHandler(
				reportingRepository = reportingRepository
			)
		)
		val params = SetSubjectGradeParams(
			quarterId = DEFAULT_RECORD_QUARTER.id,
			subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
			grade = 4,
			commit = true
		)

		useCase.execute(params).test {
			assertIs<UseCaseState.Loading<*, *>>(awaitItem())

			when (val state = awaitItem()) {
				is UseCaseState.Data -> assertEquals(Unit, state.value)
				is UseCaseState.Error -> {
					val exceptions = reportingRepository.exceptions.joinToString(
						separator = "\n"
					) { exception ->
						"${exception::class.simpleName}: ${exception.message}"
					}

					fail("Expected data state but received error=${state.error}. Exceptions:\n$exceptions")
				}
				else -> fail("Unexpected state: ${state::class.simpleName}")
			}

			awaitComplete()
		}

		assertEquals(1, repository.setGradeCalls.value.size)
		assertEquals(true, repository.setGradeCalls.value.single().commit)
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
