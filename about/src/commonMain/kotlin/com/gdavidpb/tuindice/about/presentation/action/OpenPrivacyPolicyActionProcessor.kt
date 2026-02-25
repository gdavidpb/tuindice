package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow

class OpenPrivacyPolicyActionProcessor(
	private val textProvider: AboutTextProvider,
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<About.State, About.Action.OpenPrivacyPolicy, About.Effect>() {

	override fun process(
		action: About.Action.OpenPrivacyPolicy,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.NavigateToBrowser(
				title = textProvider.privacyPolicyTitle(),
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)

		return super.process(action, sideEffect)
	}
}
