package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.isDatePassed
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toGetEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadEvaluationActionProcessor(
	private val getEvaluationAndAvailableSubjectsUseCase: GetEvaluationAndAvailableSubjectsUseCase
) : ActionProcessor<Evaluation.State, Evaluation.Action.LoadEvaluation, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.LoadEvaluation,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return getEvaluationAndAvailableSubjectsUseCase.execute(params = action.toGetEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Evaluation.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						with(useCaseState.value) {
							val selectedSubject = availableSubjects.find { subject ->
								subject.code == evaluation?.subjectCode
							}

							Evaluation.State.Content(
								availableSubjects = availableSubjects,
								selectedSubject = selectedSubject,
								type = evaluation?.type,
								date = evaluation?.date,
								isOverdue = evaluation?.date.isDatePassed(),
								grade = evaluation?.grade,
								maxGrade = evaluation?.maxGrade
							)
						}
					}

					is UseCaseState.Error -> { _ ->
						Evaluation.State.Failed
					}
				}
			}
	}
}