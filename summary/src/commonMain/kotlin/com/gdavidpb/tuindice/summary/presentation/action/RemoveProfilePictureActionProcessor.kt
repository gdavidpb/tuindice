package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class RemoveProfilePictureActionProcessor
	: ActionProcessor<Summary.State, Summary.Action.RemoveProfilePicture, Summary.Effect> {

	override suspend fun process(
		action: Summary.Action.RemoveProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		sideEffect(
			Summary.Effect.ShowRemoveProfilePictureConfirmationDialog
		)

		return emptyFlow()
	}
}
