package com.gdavidpb.tuindice.evaluations.presentation.action.list

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadEvaluationsActionProcessor(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Evaluations.State, Evaluations.Action.LoadEvaluations, Evaluations.Effect>() {

	override fun process(
		action: Evaluations.Action.LoadEvaluations,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return getEvaluationsUseCase.execute(action.activeFilters)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Evaluations.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						val evaluations = useCaseState.value

						if (evaluations.originalEvaluations.isNotEmpty())
							Evaluations.State.Content(
								originalEvaluations = evaluations.originalEvaluations,
								filteredEvaluations = evaluations.filteredEvaluations,
								availableFilters = evaluations.availableFilters,
								activeFilters = evaluations.activeFilters
							)
						else
							Evaluations.State.Empty
					}

					is UseCaseState.Error -> { _ ->
						when (val error = useCaseState.error) {
							is EvaluationsError.NoConnection ->
								sideEffect(
									Evaluations.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											resourceResolver.getString(R.string.snack_service_unavailable)
										else
											resourceResolver.getString(R.string.snack_network_unavailable)
									)
								)

							is EvaluationsError.Timeout ->
								sideEffect(
									Evaluations.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_timeout)
									)
								)

							is EvaluationsError.Unavailable ->
								sideEffect(
									Evaluations.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_service_unavailable)
									)
								)

							else ->
								sideEffect(
									Evaluations.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_default_error)
									)
								)
						}

						Evaluations.State.Failed
					}
				}
			}
	}
}