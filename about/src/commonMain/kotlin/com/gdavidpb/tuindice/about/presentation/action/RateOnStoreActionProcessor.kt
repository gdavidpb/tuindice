package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RateOnStoreActionProcessor(
	private val openStoreUseCase: OpenStoreUseCase
) : ActionProcessor<About.State, About.Action.RateOnStore, About.Effect> {
	override suspend fun process(
		action: About.Action.RateOnStore,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return openStoreUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> suspend { state ->
						sideEffect(
							About.Effect.OpenUri(
								uri = useCaseState.value
							)
						)

						state
					}

					else -> suspend { state -> state }
				}
			}
	}
}
