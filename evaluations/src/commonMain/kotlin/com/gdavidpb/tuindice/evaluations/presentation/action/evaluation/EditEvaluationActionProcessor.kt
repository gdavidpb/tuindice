package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_evaluation_not_found
import tuindice.evaluations.generated.resources.snack_evaluation_updated

class EditEvaluationActionProcessor(
	private val updateEvaluationUseCase: UpdateEvaluationUseCase
) : ActionProcessor<Evaluation.State, Evaluation.Action.ClickEditEvaluation, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.ClickEditEvaluation,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return updateEvaluationUseCase.execute(params = action.toUpdateEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state ->
						state
					}

					is UseCaseState.Data -> suspend { state: Evaluation.State ->
						val successMessage = getString(Res.string.snack_evaluation_updated)

						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = successMessage
							)
						)

						sideEffect(
							Evaluation.Effect.NavigateToEvaluations
						)

						state
					}

					is UseCaseState.Error -> suspend { state: Evaluation.State ->
						val errorMessage = when (useCaseState.error) {
							is UpdateEvaluationUseCaseError.NotFound ->
								getString(Res.string.snack_evaluation_not_found)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = errorMessage
							)
						)

						if (useCaseState.error is UpdateEvaluationUseCaseError.NotFound)
							sideEffect(Evaluation.Effect.NavigateToEvaluations)

						state
					}
				}
			}
	}
}
