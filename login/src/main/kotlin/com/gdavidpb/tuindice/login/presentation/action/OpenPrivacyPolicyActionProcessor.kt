package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow

class OpenPrivacyPolicyActionProcessor(
	private val resourceResolver: ResourceResolver
) : ActionProcessor<SignIn.State, SignIn.Action.ClickPrivacyPolicy, SignIn.Effect>() {
	override fun process(
		action: SignIn.Action.ClickPrivacyPolicy,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		sideEffect(
			SignIn.Effect.NavigateToBrowser(
				title = resourceResolver.getString(R.string.label_privacy_policy),
				url = BuildConfig.URL_APP_PRIVACY_POLICY
			)
		)

		return super.process(action, sideEffect)
	}
}