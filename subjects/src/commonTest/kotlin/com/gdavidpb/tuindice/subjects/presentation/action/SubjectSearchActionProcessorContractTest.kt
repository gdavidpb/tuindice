package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectSearchActionProcessorContractTest {
	@Test
	fun observeSubjectSearch_doesNotRefreshRemoteForShortQueries() = runTest {
		val queryFlow = MutableStateFlow("m")
		val repository = RecordingSubjectCatalogRepository()
		var state = SubjectSearch.State()
		val job = launch {
			createProcessor(repository = repository)
				.process(SubjectSearch.Action.ObserveSubjectSearch(queryFlow = queryFlow)) {}
				.collect { mutation -> state = mutation(state) }
		}

		advanceTimeBy(1_000)

		assertEquals("m", state.query)
		assertEquals(emptyList(), state.results)
		assertFalse(state.isRefreshing)
		assertEquals(emptyList(), repository.refreshCalls)
		job.cancelAndJoin()
	}

	@Test
	fun observeSubjectSearch_emitsCachedResultsBeforeDebouncedRemoteRefresh() = runTest {
		val cached = subjectSearchResult(subjectCode = "EC5333")
		val queryFlow = MutableStateFlow("micro")
		val repository = RecordingSubjectCatalogRepository(initialLocalResults = listOf(cached))
		var state = SubjectSearch.State()
		val job = launch {
			createProcessor(repository = repository)
				.process(SubjectSearch.Action.ObserveSubjectSearch(queryFlow = queryFlow)) {}
				.collect { mutation -> state = mutation(state) }
		}

		waitUntil { state.results.map { result -> result.subjectCode } == listOf("EC5333") }

		assertFalse(state.isRefreshing)
		assertEquals(emptyList(), repository.refreshCalls)

		advanceTimeBy(SubjectSearchDebounceMillis - 1)

		assertEquals(emptyList(), repository.refreshCalls)
		job.cancelAndJoin()
	}

	@Test
	fun observeSubjectSearch_cancelsPreviousRemoteRequestWhenQueryChanges() = runTest {
		val queryFlow = MutableStateFlow("")
		val repository = RecordingSubjectCatalogRepository()
		var state = SubjectSearch.State()
		val job = launch {
			createProcessor(repository = repository)
				.process(SubjectSearch.Action.ObserveSubjectSearch(queryFlow = queryFlow)) {}
				.collect { mutation -> state = mutation(state) }
		}

		queryFlow.value = "mi"
		advanceTimeBy(SubjectSearchDebounceMillis / 2)
		queryFlow.value = "micro"
		advanceTimeBy(SubjectSearchDebounceMillis + 1)
		runCurrent()

		assertEquals(listOf(SubjectSearchParams(query = "micro", limit = SubjectSearchLimit)), repository.refreshCalls)
		assertEquals("micro", state.query)
		job.cancelAndJoin()
	}

	@Test
	fun updateQuery_clearsResultsWhenQueryBecomesTooShort() = runTest {
		val processor = UpdateSubjectSearchQueryActionProcessor()

		val state = reduceState(
			initialState = SubjectSearch.State(
				query = "micro",
				results = listOf(subjectSearchResult(subjectCode = "EC5333").toItem()),
				isRefreshing = true,
				hasRemoteError = true
			),
			mutations = processor.process(SubjectSearch.Action.UpdateQuery(query = ""), sideEffect = {})
		)

		assertEquals("", state.query)
		assertEquals(emptyList(), state.results)
		assertFalse(state.isRefreshing)
		assertFalse(state.hasRemoteError)
	}
}

private class RecordingSubjectCatalogRepository(
	initialLocalResults: List<SubjectSearchResult> = emptyList(),
	private val refreshResponses: ArrayDeque<Any> = ArrayDeque()
) : SubjectCatalogRepository {
	private val localResults = MutableStateFlow(initialLocalResults)
	val refreshCalls = mutableListOf<SubjectSearchParams>()

	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> = localResults

	override suspend fun refreshSearchResults(
		query: String,
		limit: Int
	) {
		refreshCalls += SubjectSearchParams(query = query, limit = limit)
		if (refreshResponses.isEmpty()) return

		when (val next = refreshResponses.removeFirst()) {
			is Throwable -> throw next
			is List<*> -> localResults.value = next.filterIsInstance<SubjectSearchResult>()
			else -> error("Unsupported search refresh result: $next")
		}
	}
}

private fun createProcessor(
	repository: SubjectCatalogRepository
): ObserveSubjectSearchActionProcessor {
	return ObserveSubjectSearchActionProcessor(
		observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
			subjectCatalogRepository = repository,
			reportingRepository = RecordingReportingRepository()
		),
		refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
			subjectCatalogRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)
	)
}

private suspend fun reduceState(
	initialState: SubjectSearch.State,
	mutations: Flow<Mutation<SubjectSearch.State>>
): SubjectSearch.State {
	var currentState = initialState
	mutations.collect { mutation ->
		currentState = mutation(currentState)
	}
	return currentState
}

private suspend fun waitUntil(condition: () -> Boolean) {
	withTimeout(2_000) {
		while (!condition()) yield()
	}
}

private fun SubjectSearchResult.toItem(): com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem {
	return com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem(
		subjectCode = subjectCode,
		name = name,
		creditsText = "$credits UC"
	)
}

private fun subjectSearchResult(subjectCode: String): SubjectSearchResult {
	return SubjectSearchResult(
		subjectCode = subjectCode,
		name = "INT. A LAS MICROONDAS Y SUS APLICACIONES",
		credits = 3,
		gradingMode = GradingMode.NUMERIC
	)
}
