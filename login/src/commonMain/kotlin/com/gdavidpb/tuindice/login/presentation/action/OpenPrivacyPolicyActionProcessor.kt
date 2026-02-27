package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.Flow

class OpenPrivacyPolicyActionProcessor(
	private val textProvider: LoginTextProvider,
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<SignIn.State, SignIn.Action.ClickPrivacyPolicy, SignIn.Effect>() {
	override suspend fun process(
		action: SignIn.Action.ClickPrivacyPolicy,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		sideEffect(
			SignIn.Effect.NavigateToBrowser(
				title = textProvider.privacyPolicyTitle(),
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)

		return super.process(action, sideEffect)
	}
}
