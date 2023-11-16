package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.BuildConfig
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import kotlinx.coroutines.flow.Flow

class ShareAppActionProcessor(
	private val resourceResolver: ResourceResolver
) : ActionProcessor<About.State, About.Action.ShareApp, About.Effect>() {

	override fun process(
		action: About.Action.ShareApp,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.StartShare(
				text = resourceResolver.getString(
					R.string.about_share_message,
					BuildConfig.APPLICATION_ID
				),
				subject = resourceResolver.getString(R.string.app_name)
			)
		)

		return super.process(action, sideEffect)
	}
}