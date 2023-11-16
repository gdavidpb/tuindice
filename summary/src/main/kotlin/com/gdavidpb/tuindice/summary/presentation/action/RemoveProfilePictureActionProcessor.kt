package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow

class RemoveProfilePictureActionProcessor
	: ActionProcessor<Summary.State, Summary.Action.RemoveProfilePicture, Summary.Effect>() {

	override fun process(
		action: Summary.Action.RemoveProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		sideEffect(
			Summary.Effect.ShowRemoveProfilePictureConfirmationDialog
		)

		return super.process(action, sideEffect)
	}
}