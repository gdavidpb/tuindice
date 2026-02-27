package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RemoveEvaluationActionProcessor(
	private val removeEvaluationUseCase: RemoveEvaluationUseCase,
	private val textProvider: EvaluationTextProvider
) : ActionProcessor<Evaluations.State, Evaluations.Action.RemoveEvaluation, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.RemoveEvaluation,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return removeEvaluationUseCase.execute(params = action.evaluationId)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Evaluations.Effect.ShowSnackBar(
								message = textProvider.evaluationRemoved()
							)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						sideEffect(
							Evaluations.Effect.ShowSnackBar(
								message = textProvider.defaultError()
							)
						)

						state
					}
				}
			}
	}
}
