package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadEvaluationsActionProcessor(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val textProvider: EvaluationTextProvider
) : ActionProcessor<Evaluations.State, Evaluations.Action.LoadEvaluations, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.LoadEvaluations,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return getEvaluationsUseCase.execute(params = action.activeFilters)
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
						if (useCaseState.error is EvaluationsUseCaseError.NoSubjects)
							Evaluations.State.NoSubjects
						else {
							when (val error = useCaseState.error) {
								is EvaluationsUseCaseError.NoConnection ->
									sideEffect(
										Evaluations.Effect.ShowSnackBar(
											message = if (error.isNetworkAvailable)
												textProvider.serviceUnavailable()
											else
												textProvider.networkUnavailable()
										)
									)

								is EvaluationsUseCaseError.Timeout ->
									sideEffect(
										Evaluations.Effect.ShowSnackBar(
											message = textProvider.timeout()
										)
									)

								is EvaluationsUseCaseError.Unavailable ->
									sideEffect(
										Evaluations.Effect.ShowSnackBar(
											message = textProvider.serviceUnavailable()
										)
									)

								else ->
									sideEffect(
										Evaluations.Effect.ShowSnackBar(
											message = textProvider.defaultError()
										)
									)
							}

							Evaluations.State.Failed
						}
					}
				}
			}
	}
}
