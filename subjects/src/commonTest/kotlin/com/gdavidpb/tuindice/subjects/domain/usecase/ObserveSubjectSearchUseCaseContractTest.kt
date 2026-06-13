package com.gdavidpb.tuindice.subjects.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.subjectSearchResult
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSubjectSearchUseCaseContractTest {
	@Test
	fun execute_whenCatalogHasMatches_thenEmitsResultsForQueryAndLimit() = runTest {
		val results = listOf(
			subjectSearchResult(subjectCode = "EC5333"),
			subjectSearchResult(
				subjectCode = "EC5201",
				pensumStatus = AcademicPensumNodeStatus.APPROVED
			)
		)
		val repository = ControllableSubjectCatalogRepository(localResults = results)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectSearchParams(query = "micro", limit = 10)).test {
			val emitted = awaitLoadingThenData(this)
			assertEquals(results, emitted)

			awaitComplete()
		}

		assertEquals(listOf("micro"), repository.observeCalls)
		assertEquals(listOf(10), repository.observeLimits)
		assertEquals(emptyList(), repository.refreshCalls)
	}

	@Test
	fun execute_whenCatalogIsEmpty_thenEmitsEmptyResults() = runTest {
		val repository = ControllableSubjectCatalogRepository(localResults = emptyList())
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectSearchParams(query = "nada")).test {
			val emitted = awaitLoadingThenData(this)
			assertEquals(emptyList(), emitted)

			awaitComplete()
		}

		assertEquals(listOf(20), repository.observeLimits)
	}

	private fun createUseCase(
		repository: ControllableSubjectCatalogRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): ObserveSubjectSearchUseCase {
		return ObserveSubjectSearchUseCase(
			subjectCatalogRepository = repository,
			reportingRepository = reportingRepository
		)
	}
}
