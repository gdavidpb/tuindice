package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SelectSubjectTabWizardActionProcessor :
	ActionProcessor<Wizard.State, Wizard.Action.SelectSubjectTab, Wizard.Effect>() {
	override suspend fun process(
		action: Wizard.Action.SelectSubjectTab,
		sideEffect: (Wizard.Effect) -> Unit
	): Flow<Mutation<Wizard.State>> = flowOf(
		suspend { state ->
			val content = state as? Wizard.State.Content
			if (content == null) state else content.copy(selectedSubjectTab = action.tab)
		}
	)
}
