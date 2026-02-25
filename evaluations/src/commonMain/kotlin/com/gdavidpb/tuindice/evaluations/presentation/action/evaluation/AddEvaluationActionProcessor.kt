package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddEvaluationActionProcessor(
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val textProvider: EvaluationTextProvider
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
								message = textProvider.evaluationAdded()
							)
						)

						sideEffect(
							Evaluation.Effect.NavigateToEvaluations
						)

						state
					}

					is UseCaseState.Error -> { state ->
						when (useCaseState.error) {
							is AddEvaluationUseCaseError.SubjectMissed ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = textProvider.evaluationSubjectMissed()
									)
								)

							is AddEvaluationUseCaseError.TypeMissed ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = textProvider.evaluationTypeMissed()
									)
								)

							is AddEvaluationUseCaseError.MaxGradeMissed ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = textProvider.evaluationMaxGradeMissed()
									)
								)

							else ->
								sideEffect(
									Evaluation.Effect.ShowSnackBar(
										message = textProvider.defaultError()
									)
								)
						}

						state
					}
				}
			}
	}
}
