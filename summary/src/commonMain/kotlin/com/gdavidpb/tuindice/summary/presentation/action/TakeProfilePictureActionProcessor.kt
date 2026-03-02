package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow

class TakeProfilePictureActionProcessor
	: ActionProcessor<Summary.State, Summary.Action.TakeProfilePicture, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.TakeProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		sideEffect(Summary.Effect.OpenCamera)

		return super.process(action, sideEffect)
	}
}
