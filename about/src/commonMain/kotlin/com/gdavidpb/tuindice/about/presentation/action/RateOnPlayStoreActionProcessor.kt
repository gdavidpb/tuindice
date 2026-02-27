package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.OpenStorePageUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RateOnPlayStoreActionProcessor(
	private val openStorePageUseCase: OpenStorePageUseCase
) : ActionProcessor<About.State, About.Action.RateOnPlayStore, About.Effect>() {
	override suspend fun process(
		action: About.Action.RateOnPlayStore,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return openStorePageUseCase.execute(Unit)
			.map { _ -> { state -> state } }
	}
}
