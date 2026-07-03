package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.mapper.toSubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.presentation.transition.searchTransitions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectSearchMachine(
	internal val draft: SubjectSearchDraft,
	private val observeSubjectSearchUseCase: ObserveSubjectSearchUseCase,
	private val refreshSubjectSearchUseCase: RefreshSubjectSearchUseCase
) : ScreenMachine<SubjectSearch.State, SubjectSearch.Effect> {
	override fun initialState(): SubjectSearch.State = SubjectSearch.State()

	override fun define(host: MachineHost<SubjectSearch.Effect>): MachineDefinition<SubjectSearch.State> {
		return MachineDefinition.define {
			searchTransitions(machine = this@SubjectSearchMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<SubjectSearch.Effect>) {
		host.launchMachineJob {
			draft.queryFlow
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
				.collect { event -> host.processInternalEvent(event) }
		}
	}

	internal fun startRetry(host: MachineHost<SubjectSearch.Effect>, query: String) {
		host.launchMachineJob {
			if (query.trim().length < MinimumSubjectSearchQueryLength) {
				host.processInternalEvent(SubjectSearchInternalEvent.RetryCleared)
				return@launchMachineJob
			}

			host.processInternalEvent(SubjectSearchInternalEvent.RetryStarted)

			refreshSubjectSearchUseCase.execute(
				SubjectSearchParams(query = query, limit = SubjectSearchLimit)
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data ->
						host.processInternalEvent(SubjectSearchInternalEvent.RemoteSearchSucceeded)

					is UseCaseState.Error ->
						host.processInternalEvent(SubjectSearchInternalEvent.RemoteSearchFailed)

					is UseCaseState.Loading -> Unit
				}
			}
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
}

internal const val MinimumSubjectSearchQueryLength = 2
internal const val SubjectSearchLimit = 20
internal const val SubjectSearchDebounceMillis = 300L
