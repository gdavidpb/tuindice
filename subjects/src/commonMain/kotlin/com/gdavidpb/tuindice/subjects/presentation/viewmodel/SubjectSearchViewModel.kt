package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.presentation.action.MinimumSubjectSearchQueryLength
import com.gdavidpb.tuindice.subjects.presentation.action.SubjectSearchDebounceMillis
import com.gdavidpb.tuindice.subjects.presentation.action.SubjectSearchLimit
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchInternalEvent
import com.gdavidpb.tuindice.subjects.presentation.mapper.toSubjectSearchResultItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectSearchViewModel(
	private val observeSubjectSearchUseCase: ObserveSubjectSearchUseCase,
	private val refreshSubjectSearchUseCase: RefreshSubjectSearchUseCase,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SubjectSearch.State, SubjectSearch.Action, SubjectSearch.Effect>(
	name = "subject_search",
	initialState = SubjectSearch.State(),
	dispatchers = dispatchers
) {
	private val queryFlow = MutableStateFlow("")

	init {
		sendAction(SubjectSearch.Action.ObserveSubjectSearch(queryFlow = queryFlow))
	}

	fun updateQueryAction(query: String) {
		queryFlow.value = query
		sendAction(SubjectSearch.Action.UpdateQuery(query = query))
	}

	fun retryAction() {
		sendAction(SubjectSearch.Action.Retry(query = queryFlow.value))
	}

	// Degenerate machine on purpose: one formal state, every transition internal. The
	// uniformity keeps validity, telemetry, export, and the alphabet validator total.
	override fun defineMachine() = MachineDefinition.define<SubjectSearch.State> {
		from<SubjectSearch.State> {
			on<SubjectSearch.Action.ObserveSubjectSearch> { state, action ->
				startObservation(action = action)
				state
			}

			on<SubjectSearch.Action.UpdateQuery> { state, action ->
				if (action.query.trim().length < MinimumSubjectSearchQueryLength) {
					state.copy(
						query = action.query,
						results = emptyList(),
						isRefreshing = false,
						hasRemoteError = false
					)
				} else {
					state.copy(
						query = action.query,
						hasRemoteError = false
					)
				}
			}

			on<SubjectSearch.Action.Retry> { state, action ->
				startRetry(action = action)
				state
			}

			on<SubjectSearchInternalEvent.ShortQueryCleared> { state, event ->
				state.copy(
					query = event.query,
					results = emptyList(),
					isRefreshing = false,
					hasRemoteError = false
				)
			}

			on<SubjectSearchInternalEvent.LocalResultsChanged> { state, event ->
				state.copy(
					query = event.query,
					results = event.results,
					hasRemoteError = if (event.results.isNotEmpty()) false else state.hasRemoteError
				)
			}

			on<SubjectSearchInternalEvent.RemoteSearchStarted> { state, event ->
				state.copy(
					query = event.query,
					isRefreshing = true,
					hasRemoteError = false
				)
			}

			on<SubjectSearchInternalEvent.RemoteSearchSucceeded> { state, _ ->
				state.copy(
					isRefreshing = false,
					hasRemoteError = false
				)
			}

			on<SubjectSearchInternalEvent.RemoteSearchFailed> { state, _ ->
				state.copy(
					isRefreshing = false,
					hasRemoteError = state.results.isEmpty()
				)
			}

			on<SubjectSearchInternalEvent.RetryStarted> { state, _ ->
				state.copy(
					isRefreshing = true,
					hasRemoteError = false
				)
			}

			on<SubjectSearchInternalEvent.RetryCleared> { state, _ ->
				state.copy(
					isRefreshing = false,
					hasRemoteError = false
				)
			}
		}
	}

	private fun startObservation(action: SubjectSearch.Action.ObserveSubjectSearch) {
		launchMachineJob {
			action.queryFlow
				.distinctUntilChanged { old, new ->
					SubjectCatalogSearchNormalizer.normalize(old) ==
						SubjectCatalogSearchNormalizer.normalize(new)
				}
				.flatMapLatest { query ->
					if (query.trim().length < MinimumSubjectSearchQueryLength) {
						flowOf<SubjectSearchInternalEvent>(
							SubjectSearchInternalEvent.ShortQueryCleared(query = query)
						)
					} else {
						merge(
							localResults(query = query),
							remoteRefresh(query = query)
						)
					}
				}
				.collect { event -> processInternalEvent(event) }
		}
	}

	private fun localResults(query: String): Flow<SubjectSearchInternalEvent> {
		return observeSubjectSearchUseCase.execute(
			SubjectSearchParams(query = query, limit = SubjectSearchLimit)
		).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Data ->
					SubjectSearchInternalEvent.LocalResultsChanged(
						query = query,
						results = useCaseState.value.map { result ->
							result.toSubjectSearchResultItem()
						}
					)

				is UseCaseState.Loading,
				is UseCaseState.Error,
				-> null
			}
		}
	}

	private fun remoteRefresh(query: String): Flow<SubjectSearchInternalEvent> {
		return flow {
			delay(SubjectSearchDebounceMillis)

			emit(SubjectSearchInternalEvent.RemoteSearchStarted(query = query))

			refreshSubjectSearchUseCase.execute(
				SubjectSearchParams(query = query, limit = SubjectSearchLimit)
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data ->
						emit(SubjectSearchInternalEvent.RemoteSearchSucceeded)

					is UseCaseState.Error ->
						emit(SubjectSearchInternalEvent.RemoteSearchFailed)

					is UseCaseState.Loading -> Unit
				}
			}
		}
	}

	private fun startRetry(action: SubjectSearch.Action.Retry) {
		launchMachineJob {
			if (action.query.trim().length < MinimumSubjectSearchQueryLength) {
				processInternalEvent(SubjectSearchInternalEvent.RetryCleared)
				return@launchMachineJob
			}

			processInternalEvent(SubjectSearchInternalEvent.RetryStarted)

			refreshSubjectSearchUseCase.execute(
				SubjectSearchParams(query = action.query, limit = SubjectSearchLimit)
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data ->
						processInternalEvent(SubjectSearchInternalEvent.RemoteSearchSucceeded)

					is UseCaseState.Error ->
						processInternalEvent(SubjectSearchInternalEvent.RemoteSearchFailed)

					is UseCaseState.Loading -> Unit
				}
			}
		}
	}
}
