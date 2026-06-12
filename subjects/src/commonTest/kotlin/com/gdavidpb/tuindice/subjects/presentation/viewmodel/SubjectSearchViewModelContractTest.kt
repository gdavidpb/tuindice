package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.action.ObserveSubjectSearchActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.RetrySubjectSearchActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.UpdateSubjectSearchQueryActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.subjectSearchResult
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SubjectSearchViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun typingQuery_showsLocalResults_andRunsRemoteRefreshLifecycle() = runTest {
		val fixture = createFixture(
			repository = ControllableSubjectCatalogRepository(
				localResults = listOf(subjectSearchResult(subjectCode = "MAT101"))
			)
		)
		val viewModel = fixture.viewModel

		fixture.repository.blockRefresh = true

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectSearch.State(), awaitItem())

				viewModel.updateQueryAction(query = "calculo")

				awaitUntilState<SubjectSearch.State> { state ->
					state.query == "calculo" && state.results.size == 1
				}

				awaitUntilState<SubjectSearch.State> { state -> state.isRefreshing }

				fixture.repository.releaseRefresh()

				val settled = awaitUntilState<SubjectSearch.State> { state ->
					!state.isRefreshing && state.results.size == 1
				}
				assertEquals("MAT101", settled.results.single().subjectCode)
				assertEquals(false, settled.hasRemoteError)

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(listOf("calculo"), fixture.repository.refreshCalls)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun shortQuery_clearsResultsAndFlags() = runTest {
		val fixture = createFixture(
			repository = ControllableSubjectCatalogRepository(
				localResults = listOf(subjectSearchResult(subjectCode = "MAT101"))
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectSearch.State(), awaitItem())

				viewModel.updateQueryAction(query = "calculo")
				awaitUntilState<SubjectSearch.State> { state -> state.results.size == 1 }

				viewModel.updateQueryAction(query = "c")
				val cleared = awaitUntilState<SubjectSearch.State> { state ->
					state.query == "c" && state.results.isEmpty()
				}
				assertEquals(false, cleared.isRefreshing)
				assertEquals(false, cleared.hasRemoteError)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun remoteFailureWithoutLocalResults_flagsRemoteError_andRetryClearsIt() = runTest {
		val fixture = createFixture(
			repository = ControllableSubjectCatalogRepository(
				localResults = emptyList(),
				refreshResponses = ArrayDeque(listOf(IllegalStateException("boom")))
			)
		)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectSearch.State(), awaitItem())

				viewModel.updateQueryAction(query = "microondas")

				val failed = awaitUntilState<SubjectSearch.State> { state -> state.hasRemoteError }
				assertEquals(false, failed.isRefreshing)
				assertTrue(failed.results.isEmpty())

				viewModel.retryAction()

				val recovered = awaitUntilState<SubjectSearch.State> { state ->
					!state.hasRemoteError && !state.isRefreshing
				}
				assertTrue(recovered.results.isEmpty())

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(listOf("microondas", "microondas"), fixture.repository.refreshCalls)
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createFixture(
		repository: ControllableSubjectCatalogRepository
	): SubjectSearchFixture {
		val reportingRepository = RecordingReportingRepository()

		val viewModel = SubjectSearchViewModel(
			observeSubjectSearchActionProcessor = ObserveSubjectSearchActionProcessor(
				observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				),
				refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				)
			),
			updateSubjectSearchQueryActionProcessor = UpdateSubjectSearchQueryActionProcessor(),
			retrySubjectSearchActionProcessor = RetrySubjectSearchActionProcessor(
				refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)

		return SubjectSearchFixture(
			viewModel = viewModel,
			repository = repository
		)
	}
}

private data class SubjectSearchFixture(
	val viewModel: SubjectSearchViewModel,
	val repository: ControllableSubjectCatalogRepository
)
