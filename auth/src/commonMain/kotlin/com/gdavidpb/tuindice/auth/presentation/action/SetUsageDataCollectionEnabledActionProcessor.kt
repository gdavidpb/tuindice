package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetUsageDataCollectionEnabledActionProcessor(
	private val usageDataConsentRepository: UsageDataConsentRepository
) : ActionProcessor<SignIn.State, SignIn.Action.SetUsageDataCollectionEnabled, SignIn.Effect> {
	override suspend fun process(
		action: SignIn.Action.SetUsageDataCollectionEnabled,
		sideEffect: (SignIn.Effect) -> Unit
	): Flow<Mutation<SignIn.State>> {
		usageDataConsentRepository.setUsageDataCollectionEnabled(action.enabled)

		return flowOf { state ->
			when (state) {
				is SignIn.State.Idle ->
					state.copy(usageDataCollectionEnabled = action.enabled)

				is SignIn.State.LoggingIn ->
					state.copy(usageDataCollectionEnabled = action.enabled)
			}
		}
	}
}
