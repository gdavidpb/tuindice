package com.gdavidpb.tuindice.evaluations.presentation.action.list

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetEvaluationGradeActionProcessor(
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Evaluations.State, Evaluations.Action.SetEvaluationGrade, Evaluations.Effect>() {

	override fun process(
		action: Evaluations.Action.SetEvaluationGrade,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return updateEvaluationUseCase.execute(params = action.toUpdateEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Evaluations.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_evaluation_set_grade)
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