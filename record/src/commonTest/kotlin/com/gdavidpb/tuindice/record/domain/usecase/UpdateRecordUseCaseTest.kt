package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateRecordUseCaseTest {
	@Test
	fun execute_triggersRepositoryUpdate_andEmitsUnit() = runTest {
		val repository = ControllableAcademicRecordRepository()
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(1, repository.updateAcademicRecordCalls)
	}

	@Test
	fun execute_emitsTimeoutError_andReportsHandledException_whenUpdateTimesOut() = runTest {
		val repository = ControllableAcademicRecordRepository()
		repository.updateAcademicRecordThrowable = IllegalStateException("Request timed out")
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			repository = repository,
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(RecordUseCaseError.Timeout, error.error)

			awaitComplete()
		}

		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(true, reportingRepository.customKeys["is-handled"])
		assertTrue(reportingRepository.customKeys.containsKey("use-case"))
	}

	@Test
	fun execute_emitsUnmappedError_whenUpdateFailsWithUnknownException() = runTest {
		val repository = ControllableAcademicRecordRepository()
		repository.updateAcademicRecordThrowable = IllegalArgumentException("boom")
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}
	}

	private fun createUseCase(
		repository: ControllableAcademicRecordRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): UpdateRecordUseCase {
		return UpdateRecordUseCase(
			academicRecordRepository = repository,
			reportingRepository = reportingRepository,
			exceptionHandler = RecordExceptionHandler()
		)
	}
}
