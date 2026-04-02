package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.updated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetGradeActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.SetGrade, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.SetGrade,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content)
				state.copy(
					grade = action.grade,
					gradeSection = state.gradeSection.updated(
						isOverdue = state.isOverdue,
						grade = action.grade,
						maxGrade = state.maxGrade
					)
				)
			else
				state
		}
	}
}
