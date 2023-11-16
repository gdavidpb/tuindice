package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.BuildConfig
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import kotlinx.coroutines.flow.Flow

class OpenPrivacyPolicyActionProcessor(
	private val resourceResolver: ResourceResolver
) : ActionProcessor<About.State, About.Action.OpenPrivacyPolicy, About.Effect>() {

	override fun process(
		action: About.Action.OpenPrivacyPolicy,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.NavigateToBrowser(
				title = resourceResolver.getString(R.string.label_privacy_policy),
				url = BuildConfig.URL_APP_PRIVACY_POLICY
			)
		)

		return super.process(action, sideEffect)
	}
}