package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_evaluation_not_found
import tuindice.evaluations.generated.resources.snack_evaluation_set_grade

class SetEvaluationGradeActionProcessor(
	private val updateEvaluationUseCase: UpdateEvaluationUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.SetEvaluationGrade, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.SetEvaluationGrade,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return updateEvaluationUseCase.execute(params = action.toUpdateEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state ->
						state
					}

					is UseCaseState.Data -> suspend { state: Evaluations.State ->
						val successMessage = getString(Res.string.snack_evaluation_set_grade)

						sideEffect(
							Evaluations.Effect.ShowSnackBar(
								message = successMessage
							)
						)

						state
					}

					is UseCaseState.Error -> suspend { state: Evaluations.State ->
						val errorMessage = when (useCaseState.error) {
							is UpdateEvaluationUseCaseError.NotFound ->
								getString(Res.string.snack_evaluation_not_found)

							else ->
								getString(Res.string.snack_default_error)
						}

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
