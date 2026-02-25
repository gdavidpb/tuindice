package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow

class OpenTermsAndConditionsActionProcessor(
	private val textProvider: AboutTextProvider,
	private val appEnvironmentRepository: AppEnvironmentGateway
) : ActionProcessor<About.State, About.Action.OpenTermsAndConditions, About.Effect>() {

	override fun process(
		action: About.Action.OpenTermsAndConditions,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.NavigateToBrowser(
				title = textProvider.termsAndConditionsTitle(),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)

		return super.process(action, sideEffect)
	}
}
