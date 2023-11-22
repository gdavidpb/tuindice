package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EditEvaluationActionProcessor(
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Evaluation.State, Evaluation.Action.ClickEditEvaluation, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.ClickEditEvaluation,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return updateEvaluationUseCase.execute(params = action.toUpdateEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Evaluation.State.Loading
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_evaluation_updated)
							)
						)

						sideEffect(
							Evaluation.Effect.NavigateToEvaluations
						)

						state
					}

					is UseCaseState.Error -> { state ->
						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_default_error)
							)
						)

						state
					}
				}
			}
	}
}