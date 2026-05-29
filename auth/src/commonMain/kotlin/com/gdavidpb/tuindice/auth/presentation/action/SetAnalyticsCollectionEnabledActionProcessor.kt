package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetAnalyticsCollectionEnabledActionProcessor(
	private val analyticsConsentRepository: AnalyticsConsentRepository
) : ActionProcessor<SignIn.State, SignIn.Action.SetAnalyticsCollectionEnabled, SignIn.Effect>() {
	override suspend fun process(
		action: SignIn.Action.SetAnalyticsCollectionEnabled,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		analyticsConsentRepository.setAnalyticsCollectionEnabled(action.enabled)

		return flowOf { state ->
			when (state) {
				is SignIn.State.Idle ->
					state.copy(analyticsCollectionEnabled = action.enabled)

				is SignIn.State.LoggingIn ->
					state.copy(analyticsCollectionEnabled = action.enabled)
			}
		}
	}
}
