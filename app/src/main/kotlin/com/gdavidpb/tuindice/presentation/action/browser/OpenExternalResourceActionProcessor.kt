package com.gdavidpb.tuindice.presentation.action.browser

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.Flow

class OpenExternalResourceActionProcessor
	: ActionProcessor<Browser.State, Browser.Action.OpenExternalResource, Browser.Effect>() {

	override fun process(
		action: Browser.Action.OpenExternalResource,
		sideEffect: (Browser.Effect) -> Unit
	): Flow<Mutation<Browser.State>> {
		sideEffect(
			Browser.Effect.ShowExternalResourceDialog(url = action.url)
		)

		return super.process(action, sideEffect)
	}
}