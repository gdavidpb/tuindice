package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadVersionActionProcessor(
	private val loadVersionUseCase: LoadVersionUseCase
) : ActionProcessor<About.State, About.Action.LoadVersion, About.Effect>() {
	override suspend fun process(
		action: About.Action.LoadVersion,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return loadVersionUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> suspend { _ ->
						About.State.Content(
							versionText = useCaseState.value
						)
					}

					else -> suspend { _ ->
						About.State.Idle
					}
				}
			}
	}
}
