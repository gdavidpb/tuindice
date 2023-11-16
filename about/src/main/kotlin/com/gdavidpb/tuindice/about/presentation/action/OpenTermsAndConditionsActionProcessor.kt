package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.BuildConfig
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import kotlinx.coroutines.flow.Flow

class OpenTermsAndConditionsActionProcessor(
	private val resourceResolver: ResourceResolver
) : ActionProcessor<About.State, About.Action.OpenTermsAndConditions, About.Effect>() {

	override fun process(
		action: About.Action.OpenTermsAndConditions,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.NavigateToBrowser(
				title = resourceResolver.getString(R.string.label_terms_and_conditions),
				url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
			)
		)

		return super.process(action, sideEffect)
	}
}