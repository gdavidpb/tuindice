package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportBugActionProcessor(
	private val sendSupportEmailUseCase: SendSupportEmailUseCase
) : ActionProcessor<About.State, About.Action.ReportBug, About.Effect>() {
	override suspend fun process(
		action: About.Action.ReportBug,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return sendSupportEmailUseCase.execute(Unit)
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
