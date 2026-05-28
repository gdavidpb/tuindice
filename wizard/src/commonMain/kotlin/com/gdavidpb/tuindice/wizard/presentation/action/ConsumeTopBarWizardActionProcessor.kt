package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ConsumeTopBarWizardActionProcessor :
	ActionProcessor<Wizard.State, Wizard.Action.ConsumeTopBarAction, Wizard.Effect>() {
	override suspend fun process(
		action: Wizard.Action.ConsumeTopBarAction,
		sideEffect: (Wizard.Effect) -> Unit
	): Flow<Mutation<Wizard.State>> = flowOf(
		suspend { state ->
			// Top bar actions are consumed inside the wizard so they never trigger real app side effects.
			state
		}
	)
}
