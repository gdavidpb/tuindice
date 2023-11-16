package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow

class OpenTermsAndConditionsActionProcessor(
	private val resourceResolver: ResourceResolver
) : ActionProcessor<SignIn.State, SignIn.Action.ClickTermsAndConditions, SignIn.Effect>() {
	override fun process(
		action: SignIn.Action.ClickTermsAndConditions,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		sideEffect(
			SignIn.Effect.NavigateToBrowser(
				title = resourceResolver.getString(R.string.label_terms_and_conditions),
				url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
			)
		)

		return super.process(action, sideEffect)
	}
}