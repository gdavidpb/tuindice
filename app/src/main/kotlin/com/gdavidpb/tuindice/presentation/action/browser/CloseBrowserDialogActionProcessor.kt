package com.gdavidpb.tuindice.presentation.action.browser

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow

class CloseBrowserDialogActionProcessor
	: ActionProcessor<Browser.State, Browser.Action.CloseDialog, Browser.Effect>() {

	override fun process(
		action: Browser.Action.CloseDialog,
		sideEffect: (Browser.Effect) -> Unit
	): Flow<Mutation<Browser.State>> {
		sideEffect(
			Browser.Effect.CloseDialog
		)

		return super.process(action, sideEffect)
	}
}