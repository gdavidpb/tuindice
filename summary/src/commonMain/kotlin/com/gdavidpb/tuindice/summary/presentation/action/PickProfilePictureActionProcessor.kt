package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow

class PickProfilePictureActionProcessor
	: ActionProcessor<Summary.State, Summary.Action.PickProfilePicture, Summary.Effect>() {

	override fun process(
		action: Summary.Action.PickProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		sideEffect(
			Summary.Effect.OpenPicker
		)

		return super.process(action, sideEffect)
	}
}