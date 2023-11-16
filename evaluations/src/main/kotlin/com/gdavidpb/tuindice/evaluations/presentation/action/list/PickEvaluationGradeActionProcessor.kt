package com.gdavidpb.tuindice.evaluations.presentation.action.list

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PickEvaluationGradeActionProcessor(
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Evaluations.State, Evaluations.Action.ShowEvaluationGradeDialog, Evaluations.Effect>() {

	override fun process(
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

						sideEffect(
							Evaluations.Effect.ShowGradePickerDialog(
								evaluationId = evaluation.evaluationId,
								grade = evaluation.grade ?: 0.0,
								maxGrade = evaluation.maxGrade
							)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						sideEffect(
							Evaluations.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_default_error)
							)
						)

						state
					}
				}
			}
	}
}