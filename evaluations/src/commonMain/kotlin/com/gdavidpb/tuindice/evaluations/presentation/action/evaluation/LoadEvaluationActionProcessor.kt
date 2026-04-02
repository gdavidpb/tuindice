package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.extension.isDateInPast
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationTypePickerItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toGetEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationSubjectPickerItemList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadEvaluationActionProcessor(
	private val getEvaluationAndAvailableSubjectsUseCase: GetEvaluationAndAvailableSubjectsUseCase
) : ActionProcessor<Evaluation.State, Evaluation.Action.LoadEvaluation, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.LoadEvaluation,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return getEvaluationAndAvailableSubjectsUseCase.execute(params = action.toGetEvaluationParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _ ->
						Evaluation.State.Loading
					}

					is UseCaseState.Data -> suspend { _ ->
						with(useCaseState.value) {
							val selectedSubject = availableSubjects.find { subject ->
								subject.id == evaluation?.subjectId
							}
							val isOverdue = evaluation?.let { loadedEvaluation ->
								loadedEvaluation.scheduleMode == EvaluationScheduleMode.DATED &&
									loadedEvaluation.date.isDateInPast()
							} ?: false

							Evaluation.State.Content(
								evaluationId = action.evaluationId,
								subjectItems = availableSubjects.toEvaluationSubjectPickerItemList(
									selectedSubject = selectedSubject
								),
								selectedSubject = selectedSubject,
								type = evaluation?.type,
								typeItems = getEvaluationTypePickerItemList(
									selectedType = evaluation?.type
								),
								scheduleMode = evaluation?.scheduleMode ?: EvaluationScheduleMode.CONTINUOUS,
								date = evaluation?.date,
								isOverdue = isOverdue,
								grade = evaluation?.grade,
								maxGrade = evaluation?.maxGrade,
								gradeSection = getEvaluationGradeSectionItem(
									isOverdue = isOverdue,
									grade = evaluation?.grade,
									maxGrade = evaluation?.maxGrade
								)
							)
						}
					}

					is UseCaseState.Error -> suspend { _ ->
						Evaluation.State.Failed
					}
				}
			}
	}
}
