package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow

class PickGradeActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.ClickGrade, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.ClickGrade,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		sideEffect(
			Evaluation.Effect.ShowGradePickerDialog(
				grade = action.grade,
				maxGrade = action.maxGrade
			)
		)

		return super.process(action, sideEffect)
	}
}