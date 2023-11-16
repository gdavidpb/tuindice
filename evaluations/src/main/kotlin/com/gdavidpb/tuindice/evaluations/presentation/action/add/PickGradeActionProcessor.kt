package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow

class PickGradeActionProcessor
	: ActionProcessor<AddEvaluation.State, AddEvaluation.Action.ClickGrade, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.ClickGrade,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		sideEffect(
			AddEvaluation.Effect.ShowGradePickerDialog(
				grade = action.grade,
				maxGrade = action.maxGrade
			)
		)

		return super.process(action, sideEffect)
	}
}