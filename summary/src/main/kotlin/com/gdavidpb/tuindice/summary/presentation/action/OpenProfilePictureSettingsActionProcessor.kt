package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OpenProfilePictureSettingsActionProcessor
	: ActionProcessor<Summary.State, Summary.Action.OpenProfilePictureSettings, Summary.Effect>() {

	override fun process(
		action: Summary.Action.OpenProfilePictureSettings,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return flowOf { state ->
			Summary.Effect.ShowProfilePictureSettingsDialog(
				showRemove = state is Summary.State.Content && state.profilePictureUrl.isNotEmpty()
			)

			state
		}
	}
}