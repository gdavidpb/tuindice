package com.gdavidpb.tuindice.presentation.action.browser

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow

class ConfirmOpenExternalResourceActionProcessor
	: ActionProcessor<Browser.State, Browser.Action.ConfirmOpenExternalResource, Browser.Effect>() {

	override fun process(
		action: Browser.Action.ConfirmOpenExternalResource,
		sideEffect: (Browser.Effect) -> Unit
	): Flow<Mutation<Browser.State>> {
		sideEffect(
			Browser.Effect.OpenExternalResource(url = action.url)
		)

		return super.process(action, sideEffect)
	}
}