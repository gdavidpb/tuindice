package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OpenUrlActionProcessor(
	private val openExternalUrlUseCase: OpenExternalUrlUseCase
) : ActionProcessor<About.State, About.Action.OpenUrl, About.Effect> {
	override suspend fun process(
		action: About.Action.OpenUrl,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return openExternalUrlUseCase.execute(action.url)
			.map { _ -> { state -> state } }
	}
}
