package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RemoveEvaluationActionProcessor(
	private val removeEvaluationUseCase: RemoveEvaluationUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Evaluations.State, Evaluations.Action.RemoveEvaluation, Evaluations.Effect>() {

	override fun process(
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
								message = resourceResolver.getString(R.string.snack_evaluation_removed)
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