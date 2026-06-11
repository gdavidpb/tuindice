package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSubjectChartsVisibleWizardActionProcessor :
	ActionProcessor<Wizard.State, Wizard.Action.SetSubjectChartsVisible, Wizard.Effect> {
	override suspend fun process(
		action: Wizard.Action.SetSubjectChartsVisible,
		sideEffect: (Wizard.Effect) -> Unit
	): Flow<Mutation<Wizard.State>> = flowOf(
		suspend { state ->
			val content = state as? Wizard.State.Content ?: return@flowOf state
			when {
				action.isVisible && content.currentStep.id == WizardStepId.SubjectDetail ->
					content.goTo(WizardStepId.SubjectCharts)

				!action.isVisible && content.currentStep.id == WizardStepId.SubjectCharts ->
					content.goTo(WizardStepId.SubjectDetail)

				else ->
					state
			}
		}
	)
}
