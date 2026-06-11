package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.updated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private const val MIN_EVALUATION_GRADE = 0.0

class SetMaxGradeActionProcessor
	:
	ActionProcessor<Evaluation.State, Evaluation.Action.SetMaxGrade, Evaluation.Effect> {

	override suspend fun process(
		action: Evaluation.Action.SetMaxGrade,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content) {
				val maxGrade = action.maxGrade.takeIf { it > MIN_EVALUATION_GRADE }

				state.copy(
					maxGrade = maxGrade,
					gradeSection = state.gradeSection.updated(
						isOverdue = state.isOverdue,
						grade = state.grade,
						maxGrade = maxGrade
					)
				)
			}
			else
				state
		}
	}
}
