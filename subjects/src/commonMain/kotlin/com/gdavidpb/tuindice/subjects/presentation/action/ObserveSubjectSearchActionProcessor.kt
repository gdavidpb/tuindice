package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.mapper.toSubjectSearchResultItem
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
class ObserveSubjectSearchActionProcessor(
	private val observeSubjectSearchUseCase: ObserveSubjectSearchUseCase,
	private val refreshSubjectSearchUseCase: RefreshSubjectSearchUseCase
) : ActionProcessor<
	SubjectSearch.State,
	SubjectSearch.Action.ObserveSubjectSearch,
	SubjectSearch.Effect
	>() {
	override suspend fun process(
		action: SubjectSearch.Action.ObserveSubjectSearch,
		sideEffect: (SubjectSearch.Effect) -> Unit
	): Flow<Mutation<SubjectSearch.State>> {
		return action.queryFlow
			.distinctUntilChanged { old, new ->
				SubjectCatalogSearchNormalizer.normalize(old) == SubjectCatalogSearchNormalizer.normalize(new)
			}
			.flatMapLatest { query ->
				if (query.trim().length < MinimumSubjectSearchQueryLength) {
					flowOf(
						suspend { state: SubjectSearch.State ->
							state.copy(
								query = query,
								results = emptyList(),
								isRefreshing = false,
								hasRemoteError = false
							)
						}
					)
				} else {
					merge(
						observeLocalResults(query),
						refreshRemoteResults(query, debounce = true)
					)
				}
			}
	}

	private fun observeLocalResults(query: String): Flow<Mutation<SubjectSearch.State>> {
		return observeSubjectSearchUseCase.execute(
			SubjectSearchParams(query = query, limit = SubjectSearchLimit)
		).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Data ->
					suspend { state: SubjectSearch.State ->
						val results = useCaseState.value.map { result -> result.toSubjectSearchResultItem() }
						state.copy(
							query = query,
							results = results,
							hasRemoteError = if (results.isNotEmpty()) false else state.hasRemoteError
						)
					}

				is UseCaseState.Loading,
				is UseCaseState.Error,
				-> null
			}
		}
	}

	private fun refreshRemoteResults(
		query: String,
		debounce: Boolean
	): Flow<Mutation<SubjectSearch.State>> {
		return flow {
			if (debounce) delay(SubjectSearchDebounceMillis)

			emit(
				suspend { state: SubjectSearch.State ->
					state.copy(
						query = query,
						isRefreshing = true,
						hasRemoteError = false
					)
				}
			)

			refreshSubjectSearchUseCase.execute(
				SubjectSearchParams(query = query, limit = SubjectSearchLimit)
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data ->
						emit(
							suspend { state: SubjectSearch.State ->
								state.copy(
									isRefreshing = false,
									hasRemoteError = false
								)
							}
						)

					is UseCaseState.Error ->
						emit(
							suspend { state: SubjectSearch.State ->
								state.copy(
									isRefreshing = false,
									hasRemoteError = state.results.isEmpty()
								)
							}
						)

					is UseCaseState.Loading -> Unit
				}
			}
		}
	}
}
