package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.utils.isDateInPast
import com.gdavidpb.tuindice.evaluations.presentation.mapper.updated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetDateActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.SetDate, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.SetDate,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content) {
				val isOverdue = action.date.isDateInPast()

				state.copy(
					scheduleMode = if (action.date == null) {
						EvaluationScheduleMode.CONTINUOUS
					} else {
						EvaluationScheduleMode.DATED
					},
					date = action.date,
					isOverdue = isOverdue,
					gradeSection = state.gradeSection.updated(
						isOverdue = isOverdue,
						grade = state.grade,
						maxGrade = state.maxGrade
					)
				)
			}
			else
				state
		}
	}
}
