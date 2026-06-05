package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
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
			val content = state as? Wizard.State.Content
			if (content == null) {
				state
			} else {
				when (action.topBarAction) {
					TopBarAction.RecordTermSelectionAction ->
						if (content.currentStep.id.isRecordStep) {
							content.copy(isRecordTermSelectionVisible = true)
						} else {
							content
						}

					else ->
						content
				}
			}
		}
	)
}

private val WizardStepId.isRecordStep: Boolean
	get() = this == WizardStepId.Record || this == WizardStepId.RecordActions
