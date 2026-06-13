package com.gdavidpb.tuindice.subjects.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailLoad
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import com.gdavidpb.tuindice.subjects.testing.RecordingSubjectStatsRepository
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoadSubjectDetailUseCaseContractTest {
	@Test
	fun execute_whenLocalCacheIsFresh_thenEmitsDataWithoutRemoteRefresh() = runTest {
		val cached = readySubjectDetail()
		val repository = RecordingSubjectStatsRepository(freshResult = cached)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT101")).test {
			val load = awaitLoadingThenData(this)
			assertEquals(SubjectDetailLoad.Data(cached), load)

			awaitComplete()
		}

		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(emptyList(), repository.refreshCalls)
	}

	@Test
	fun execute_whenLocalCacheIsMissing_thenEmitsLoadingRemoteThenRefreshedData() = runTest {
		val refreshed = readySubjectDetail()
		val repository = RecordingSubjectStatsRepository(
			results = listOf(refreshed)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT101")).test {
			val first = awaitLoadingThenData(this)
			assertEquals(SubjectDetailLoad.LoadingRemote, first)

			val second = assertIs<UseCaseState.Data<SubjectDetailLoad>>(awaitItem())
			assertEquals(SubjectDetailLoad.Data(refreshed), second.value)

			awaitComplete()
		}

		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(listOf("MAT101"), repository.refreshCalls)
	}

	@Test
	fun execute_whenRefreshedSubjectIsUnavailable_thenEmitsUnavailableResult() = runTest {
		val unavailable = SubjectDetailResult.Unavailable(
			subjectCode = "MAT404",
			expiresAt = 123L
		)
		val repository = RecordingSubjectStatsRepository(
			results = listOf(unavailable)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT404")).test {
			val first = awaitLoadingThenData(this)
			assertEquals(SubjectDetailLoad.LoadingRemote, first)

			val second = assertIs<UseCaseState.Data<SubjectDetailLoad>>(awaitItem())
			assertEquals(SubjectDetailLoad.Data(unavailable), second.value)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenRemoteRefreshFails_thenEmitsLoadingRemoteThenUnhandledError() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val repository = RecordingSubjectStatsRepository(
			results = listOf(IllegalStateException("boom"))
		)
		val useCase = createUseCase(
			repository = repository,
			reportingRepository = reportingRepository
		)

		useCase.execute(SubjectDetailParams(subjectCode = "MAT101")).test {
			val first = awaitLoadingThenData(this)
			assertEquals(SubjectDetailLoad.LoadingRemote, first)

			val error = assertIs<UseCaseState.Error<*>>(awaitItem())
			assertNull(error.error)

			awaitComplete()
		}

		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(false, reportingRepository.customKeys["is-handled"])
		assertTrue(reportingRepository.customKeys.containsKey("use-case"))
	}

	private fun createUseCase(
		repository: RecordingSubjectStatsRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): LoadSubjectDetailUseCase {
		return LoadSubjectDetailUseCase(
			subjectStatsRepository = repository,
			reportingRepository = reportingRepository
		)
	}
}
