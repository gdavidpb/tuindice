package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PickMaxGradeActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.ClickMaxGrade, Evaluation.Effect> {

	override suspend fun process(
		action: Evaluation.Action.ClickMaxGrade,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		sideEffect(
			Evaluation.Effect.NavigateToMaxGradePickerDialog(
				evaluationName = action.evaluationName,
				subjectCode = action.subjectCode,
				maxGrade = action.maxGrade
			)
		)

		return emptyFlow()
	}
}
