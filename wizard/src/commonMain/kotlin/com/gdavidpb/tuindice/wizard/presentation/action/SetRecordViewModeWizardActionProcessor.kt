package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.wizard.presentation.contract.CURRENT_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.HISTORICAL_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetRecordViewModeWizardActionProcessor :
	ActionProcessor<Wizard.State, Wizard.Action.SetRecordViewMode, Wizard.Effect>() {
	override suspend fun process(
		action: Wizard.Action.SetRecordViewMode,
		sideEffect: (Wizard.Effect) -> Unit
	): Flow<Mutation<Wizard.State>> = flowOf(
		suspend { state ->
			val content = state as? Wizard.State.Content
			if (content == null) {
				state
			} else {
				content.copy(
					recordViewMode = action.viewMode,
					selectedTermId = when (action.viewMode) {
						RecordViewMode.Official -> HISTORICAL_TERM_ID
						RecordViewMode.Working -> CURRENT_TERM_ID
					}
				)
			}
		}
	)
}
