package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RetrySubjectSearchActionProcessor(
	private val refreshSubjectSearchUseCase: RefreshSubjectSearchUseCase
) : ActionProcessor<
	SubjectSearch.State,
	SubjectSearch.Action.Retry,
	SubjectSearch.Effect
	>() {
	override suspend fun process(
		action: SubjectSearch.Action.Retry,
		sideEffect: (SubjectSearch.Effect) -> Unit
	): Flow<Mutation<SubjectSearch.State>> {
		val query = action.query
		if (query.trim().length < MinimumSubjectSearchQueryLength) {
			return flow {
				emit(
					suspend { state: SubjectSearch.State ->
						state.copy(
							isRefreshing = false,
							hasRemoteError = false
						)
					}
				)
			}
		}

		return flow {
			emit(
				suspend { state: SubjectSearch.State ->
					state.copy(
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
