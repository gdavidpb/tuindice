package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetUsageDataCollectionEnabledActionProcessor(
	private val usageDataConsentRepository: UsageDataConsentRepository
) : ActionProcessor<About.State, About.Action.SetUsageDataCollectionEnabled, About.Effect>() {
	override suspend fun process(
		action: About.Action.SetUsageDataCollectionEnabled,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		usageDataConsentRepository.setUsageDataCollectionEnabled(action.enabled)

		return flowOf { state ->
			if (state is About.State.Content) {
				state.copy(usageDataCollectionEnabled = action.enabled)
			} else {
				state
			}
		}
	}
}
