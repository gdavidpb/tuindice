package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow

class OpenUrlActionProcessor
	: ActionProcessor<About.State, About.Action.OpenUrl, About.Effect>() {

	override fun process(
		action: About.Action.OpenUrl,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.StartBrowser(url = action.url)
		)

		return super.process(action, sideEffect)
	}
}