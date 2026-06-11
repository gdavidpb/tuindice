package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.title_privacy_policy

class OpenPrivacyPolicyActionProcessor(
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<SignIn.State, SignIn.Action.ClickPrivacyPolicy, SignIn.Effect> {
	override suspend fun process(
		action: SignIn.Action.ClickPrivacyPolicy,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		sideEffect(
			SignIn.Effect.NavigateToBrowser(
				title = getString(Res.string.title_privacy_policy),
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)

		return emptyFlow()
	}
}
