package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PickEvaluationGradeActionProcessor(
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val textProvider: EvaluationTextProvider
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

					is UseCaseState.Data -> { state ->
						val evaluation = useCaseState.value

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
									message = textProvider.defaultError()
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
