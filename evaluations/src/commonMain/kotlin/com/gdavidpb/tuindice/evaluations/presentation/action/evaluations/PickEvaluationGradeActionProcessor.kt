package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_default_error

class PickEvaluationGradeActionProcessor(
	private val getEvaluationUseCase: GetEvaluationUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.ShowEvaluationGradeDialog, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.ShowEvaluationGradeDialog,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return getEvaluationUseCase.execute(params = action.evaluationId)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> run {
						val evaluation = useCaseState.value
						val errorMessage = getString(Res.string.snack_default_error)

						suspend { state: Evaluations.State ->
							if (evaluation != null)
								sideEffect(
									Evaluations.Effect.NavigateToGradePickerDialog(
										evaluationId = evaluation.id,
										grade = evaluation.grade ?: 0.0,
										maxGrade = evaluation.maxGrade
									)
								)
							else
								sideEffect(
									Evaluations.Effect.ShowSnackBar(
										message = errorMessage
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
