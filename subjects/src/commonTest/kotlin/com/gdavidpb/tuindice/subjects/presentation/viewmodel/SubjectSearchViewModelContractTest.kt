package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchDraft
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchMachine
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.subjectSearchResult
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun updateQuery_withANormalisationEqualEdit_keepsTheTypedQueryWhenResultsArrive() = runTest {
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
				viewModel.updateQueryAction(query = "calculo ")

				awaitUntilState<SubjectSearch.State> { state -> state.results.size == 1 }

				val refreshing = awaitUntilState<SubjectSearch.State> { state -> state.isRefreshing }
				assertEquals("calculo ", refreshing.query)

				fixture.repository.releaseRefresh()

				val settled = awaitUntilState<SubjectSearch.State> { state -> !state.isRefreshing }
				assertEquals("calculo ", settled.query)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun resultsOfASupersededQuery_areIgnored() = runTest {
		val repository = PerQuerySubjectCatalogRepository()
		val fixture = createFixture(repository = repository)
		val viewModel = fixture.viewModel

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SubjectSearch.State(), awaitItem())

				viewModel.updateQueryAction(query = "calculo")
				awaitUntilState<SubjectSearch.State> { state -> state.query == "calculo" }

				viewModel.updateQueryAction(query = "fisica")
				awaitUntilState<SubjectSearch.State> { state -> state.query == "fisica" }

				repository.emit(query = "calculo", results = listOf(subjectSearchResult(subjectCode = "MAT101")))
				repository.emit(query = "fisica", results = listOf(subjectSearchResult(subjectCode = "FIS101")))

				val settled = awaitUntilState<SubjectSearch.State> { state -> state.results.isNotEmpty() }
				assertEquals("fisica", settled.query)
				assertEquals("FIS101", settled.results.single().subjectCode)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun <R : SubjectCatalogRepository> createFixture(
		repository: R
	): SubjectSearchFixture<R> {
		val reportingRepository = RecordingReportingRepository()

		val viewModel = SubjectSearchViewModel(
			screenMachine = SubjectSearchMachine(
				draft = SubjectSearchDraft(),
				observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				),
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

private data class SubjectSearchFixture<R : SubjectCatalogRepository>(
	val viewModel: SubjectSearchViewModel,
	val repository: R
)

private class PerQuerySubjectCatalogRepository : SubjectCatalogRepository {
	private val flows = mutableMapOf<String, MutableSharedFlow<List<SubjectSearchResult>>>()

	private fun flowFor(query: String) = flows.getOrPut(query) { MutableSharedFlow(replay = 1) }

	fun emit(query: String, results: List<SubjectSearchResult>) {
		flowFor(query).tryEmit(results)
	}

	override fun observeSearchResults(query: String, limit: Int): Flow<List<SubjectSearchResult>> =
		flowFor(query)

	override suspend fun refreshSearchResults(query: String, limit: Int) = Unit
}
