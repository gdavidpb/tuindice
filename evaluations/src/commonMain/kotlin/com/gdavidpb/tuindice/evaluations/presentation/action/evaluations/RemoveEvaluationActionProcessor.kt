package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_evaluation_removed

class RemoveEvaluationActionProcessor(
	private val removeEvaluationUseCase: RemoveEvaluationUseCase
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

					is UseCaseState.Data -> run {
						val successMessage = getString(Res.string.snack_evaluation_removed)

						suspend { state: Evaluations.State ->
							sideEffect(
								Evaluations.Effect.ShowSnackBar(
									message = successMessage
								)
							)

							state
						}
					}

					is UseCaseState.Error -> run {
						val errorMessage = getString(Res.string.snack_default_error)

						suspend { state: Evaluations.State ->
							sideEffect(
								Evaluations.Effect.ShowSnackBar(
									message = errorMessage
								)
							)

							state
						}
					}
				}
			}
	}
}
