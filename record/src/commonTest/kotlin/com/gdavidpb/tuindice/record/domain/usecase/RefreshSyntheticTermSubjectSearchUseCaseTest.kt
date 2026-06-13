package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.RefreshSyntheticTermSubjectSearchParams
import com.gdavidpb.tuindice.record.testing.ControllableSyntheticTermCreationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshSyntheticTermSubjectSearchUseCaseTest {
	@Test
	fun execute_delegatesQueryToRepository_andEmitsUnit() = runTest {
		val repository = ControllableSyntheticTermCreationRepository()
		val useCase = createUseCase(repository)

		useCase.execute(
			RefreshSyntheticTermSubjectSearchParams(query = "matemáticas")
		).test {
			assertEquals(Unit, awaitLoadingThenData(this))

			awaitComplete()
		}

		assertEquals(listOf("matemáticas"), repository.refreshCalls)
	}

	@Test
	fun execute_emitsTimeoutError_whenSearchRefreshTimesOut() = runTest {
		val repository = ControllableSyntheticTermCreationRepository()
		repository.refreshSearchThrowable = IllegalStateException("Request timed out")
		val useCase = createUseCase(repository)

		useCase.execute(
			RefreshSyntheticTermSubjectSearchParams(query = "fs1111")
		).test {
			val error = awaitLoadingThenError(this)
			assertEquals(RecordUseCaseError.Timeout, error.error)

			awaitComplete()
		}

		assertEquals(listOf("fs1111"), repository.refreshCalls)
	}

	private fun createUseCase(
		repository: ControllableSyntheticTermCreationRepository
	): RefreshSyntheticTermSubjectSearchUseCase {
		return RefreshSyntheticTermSubjectSearchUseCase(
			repository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)
	}
}
