package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow

class PickMaxGradeActionProcessor
	: ActionProcessor<AddEvaluation.State, AddEvaluation.Action.ClickMaxGrade, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.ClickMaxGrade,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		sideEffect(
			AddEvaluation.Effect.ShowMaxGradePickerDialog(
				maxGrade = action.maxGrade
			)
		)

		return super.process(action, sideEffect)
	}
}