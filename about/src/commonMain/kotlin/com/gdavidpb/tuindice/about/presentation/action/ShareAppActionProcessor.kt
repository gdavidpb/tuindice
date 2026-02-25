package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow

class ShareAppActionProcessor(
	private val textProvider: AboutTextProvider
) : ActionProcessor<About.State, About.Action.ShareApp, About.Effect>() {

	override fun process(
		action: About.Action.ShareApp,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.StartShare(
				text = textProvider.shareMessage(),
				subject = textProvider.shareSubject()
			)
		)

		return super.process(action, sideEffect)
	}
}
