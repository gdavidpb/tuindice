package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.Flow

class OpenTermsAndConditionsActionProcessor(
	private val textProvider: LoginTextProvider,
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<SignIn.State, SignIn.Action.ClickTermsAndConditions, SignIn.Effect>() {
	override suspend fun process(
		action: SignIn.Action.ClickTermsAndConditions,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		sideEffect(
			SignIn.Effect.NavigateToBrowser(
				title = textProvider.termsAndConditionsTitle(),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)

		return super.process(action, sideEffect)
	}
}
