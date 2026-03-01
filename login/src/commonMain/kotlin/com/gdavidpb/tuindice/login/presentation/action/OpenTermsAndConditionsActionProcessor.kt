package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.title_terms_and_conditions

class OpenTermsAndConditionsActionProcessor(
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<SignIn.State, SignIn.Action.ClickTermsAndConditions, SignIn.Effect>() {
	override suspend fun process(
		action: SignIn.Action.ClickTermsAndConditions,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		sideEffect(
			SignIn.Effect.NavigateToBrowser(
				title = getString(Res.string.title_terms_and_conditions),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)

		return super.process(action, sideEffect)
	}
}
