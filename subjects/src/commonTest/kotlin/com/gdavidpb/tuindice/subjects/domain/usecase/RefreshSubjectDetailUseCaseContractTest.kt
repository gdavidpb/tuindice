package com.gdavidpb.tuindice.subjects.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import com.gdavidpb.tuindice.subjects.testing.RecordingSubjectStatsRepository
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RefreshSubjectDetailUseCaseContractTest {
	@Test
	fun execute_whenRefreshSucceeds_thenEmitsRefreshedResultWithoutCacheLookup() = runTest {
		val refreshed = readySubjectDetail()
		val repository = RecordingSubjectStatsRepository(
			freshResult = readySubjectDetail(subjectCode = "STALE"),
			results = listOf(refreshed)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT101")).test {
			val result = awaitLoadingThenData(this)
			assertEquals(refreshed, result)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.freshCalls)
		assertEquals(listOf("MAT101"), repository.refreshCalls)
	}

	@Test
	fun execute_whenRefreshReturnsUnavailable_thenEmitsUnavailableResult() = runTest {
		val unavailable = SubjectDetailResult.Unavailable(
			subjectCode = "MAT404",
			expiresAt = 123L
		)
		val repository = RecordingSubjectStatsRepository(
			results = listOf(unavailable)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT404")).test {
			val result = awaitLoadingThenData(this)
			assertEquals(unavailable, result)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenRefreshFails_thenEmitsUnhandledErrorAndReportsException() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val repository = RecordingSubjectStatsRepository(
			results = listOf(IllegalStateException("boom"))
		)
		val useCase = createUseCase(
			repository = repository,
			reportingRepository = reportingRepository
		)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT101")).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}

		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(false, reportingRepository.customKeys["is-handled"])
	}

	private fun createUseCase(
		repository: RecordingSubjectStatsRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): RefreshSubjectDetailUseCase {
		return RefreshSubjectDetailUseCase(
			subjectStatsRepository = repository,
			reportingRepository = reportingRepository
		)
	}
}
