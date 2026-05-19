package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.error_evaluation_max_grade_missed
import tuindice.evaluations.generated.resources.error_evaluation_subject_missed
import tuindice.evaluations.generated.resources.error_evaluation_type_missed
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_evaluation_already_exists
import tuindice.evaluations.generated.resources.snack_evaluation_added

class AddEvaluationActionProcessor(
	private val addEvaluationUseCase: AddEvaluationUseCase
) : ActionProcessor<Evaluation.State, Evaluation.Action.ClickAddEvaluation, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.ClickAddEvaluation,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return addEvaluationUseCase.execute(params = action.toAddEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state ->
						if (state is Evaluation.State.Content)
							state.copy(isSubmitting = true)
						else
							state
					}

					is UseCaseState.Data -> suspend { state ->
						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = getString(Res.string.snack_evaluation_added)
							)
						)

						sideEffect(
							Evaluation.Effect.NavigateToEvaluations
						)

						if (state is Evaluation.State.Content)
							state.copy(isSubmitting = false)
						else
							state
					}

					is UseCaseState.Error -> suspend { state: Evaluation.State ->
						val errorMessage = when (useCaseState.error) {
							is AddEvaluationUseCaseError.AlreadyExists ->
								getString(Res.string.snack_evaluation_already_exists)

							is AddEvaluationUseCaseError.AttemptMissed ->
								getString(Res.string.error_evaluation_subject_missed)

							is AddEvaluationUseCaseError.TypeMissed ->
								getString(Res.string.error_evaluation_type_missed)

							is AddEvaluationUseCaseError.MaxGradeMissed ->
								getString(Res.string.error_evaluation_max_grade_missed)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect(
							Evaluation.Effect.ShowSnackBar(
								message = errorMessage
							)
						)

						if (state is Evaluation.State.Content)
							state.copy(isSubmitting = false)
						else
							state
					}
				}
			}
	}
}
