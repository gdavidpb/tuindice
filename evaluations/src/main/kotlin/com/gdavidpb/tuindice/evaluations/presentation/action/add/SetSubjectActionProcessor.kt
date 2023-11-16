package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSubjectActionProcessor
	: ActionProcessor<AddEvaluation.State, AddEvaluation.Action.SetSubject, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.SetSubject,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		return flowOf { state ->
			if (state is AddEvaluation.State.Content)
				state.copy(
					subject = action.subject
				)
			else
				state
		}
	}
}