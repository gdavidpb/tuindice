package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetAnalyticsCollectionEnabledActionProcessor(
	private val analyticsConsentRepository: AnalyticsConsentRepository
) : ActionProcessor<About.State, About.Action.SetAnalyticsCollectionEnabled, About.Effect>() {
	override suspend fun process(
		action: About.Action.SetAnalyticsCollectionEnabled,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		analyticsConsentRepository.setAnalyticsCollectionEnabled(action.enabled)

		return flowOf { state ->
			if (state is About.State.Content) {
				state.copy(analyticsCollectionEnabled = action.enabled)
			} else {
				state
			}
		}
	}
}
