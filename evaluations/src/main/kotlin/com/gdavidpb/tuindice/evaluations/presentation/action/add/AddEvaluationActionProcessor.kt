package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddEvaluationActionProcessor(
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<AddEvaluation.State, AddEvaluation.Action.ClickDone, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.ClickDone,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		return addEvaluationUseCase.execute(params = action.toAddEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						AddEvaluation.State.Loading
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							AddEvaluation.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_evaluation_added)
							)
						)

						sideEffect(
							AddEvaluation.Effect.NavigateToEvaluations
						)

						state
					}

					is UseCaseState.Error -> { state ->
						when (useCaseState.error) {
							is AddEvaluationError.SubjectMissed ->
								sideEffect(
									AddEvaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.error_evaluation_subject_missed)
									)
								)

							is AddEvaluationError.TypeMissed ->
								sideEffect(
									AddEvaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.error_evaluation_type_missed)
									)
								)

							is AddEvaluationError.MaxGradeMissed ->
								sideEffect(
									AddEvaluation.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.error_evaluation_max_grade_missed)
									)
								)

							else ->
								sideEffect(
									AddEvaluation.Effect.ShowSnackBar(
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