package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSubjectActionProcessor
	: ActionProcessor<Evaluation.State, Evaluation.Action.SetSubject, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.SetSubject,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return flowOf { state ->
			if (state is Evaluation.State.Content)
				state.copy(
					selectedSubject = action.subject
				)
			else
				state
		}
	}
}