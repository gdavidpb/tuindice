package com.gdavidpb.tuindice.subjects.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RefreshSubjectSearchUseCaseContractTest {
	@Test
	fun execute_whenRefreshSucceeds_thenDelegatesQueryAndLimitToCatalog() = runTest {
		val repository = ControllableSubjectCatalogRepository()
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectSearchParams(query = "comunicaciones", limit = 5)).test {
			awaitLoadingThenData(this)

			awaitComplete()
		}

		assertEquals(listOf("comunicaciones"), repository.refreshCalls)
		assertEquals(listOf(5), repository.refreshLimits)
		assertEquals(emptyList(), repository.observeCalls)
	}

	@Test
	fun execute_whenRefreshFails_thenEmitsUnhandledErrorAndReportsException() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val repository = ControllableSubjectCatalogRepository(
			refreshResponses = ArrayDeque(listOf(IllegalStateException("boom")))
		)
		val useCase = createUseCase(
			repository = repository,
			reportingRepository = reportingRepository
		)

		useCase.execute(SubjectSearchParams(query = "micro")).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}

		assertEquals(listOf("micro"), repository.refreshCalls)
		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(false, reportingRepository.customKeys["is-handled"])
		assertTrue(reportingRepository.customKeys.containsKey("use-case"))
	}

	private fun createUseCase(
		repository: ControllableSubjectCatalogRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): RefreshSubjectSearchUseCase {
		return RefreshSubjectSearchUseCase(
			subjectCatalogRepository = repository,
			reportingRepository = reportingRepository
		)
	}
}
