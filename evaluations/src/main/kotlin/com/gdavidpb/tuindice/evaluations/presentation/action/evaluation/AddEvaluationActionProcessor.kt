package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class AddEvaluationActionProcessor(
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Evaluation.State, Evaluation.Action.ClickAddEvaluation, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.ClickAddEvaluation,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return addEvaluationUseCase.execute(params = action.toAddEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Evaluation.State.Loading
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_evaluation_added)
							)
						)

						sideEffect(
							Evaluation.Effect.NavigateToEvaluations
						)

						state
					}

					is UseCaseState.Error -> { state ->
						when (useCaseState.error) {
							is AddEvaluationError.SubjectMissed ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.error_evaluation_subject_missed)
									)
								)

							is AddEvaluationError.TypeMissed ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.error_evaluation_type_missed)
									)
								)

							is AddEvaluationError.MaxGradeMissed ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.error_evaluation_max_grade_missed)
									)
								)

							else ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_default_error)
									)
								)
						}

						state
					}
				}
			}
	}
}