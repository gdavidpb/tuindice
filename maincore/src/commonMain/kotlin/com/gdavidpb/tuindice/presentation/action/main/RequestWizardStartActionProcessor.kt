package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.wizard.domain.usecase.ShouldStartWizardUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestWizardStartActionProcessor(
	private val shouldStartWizardUseCase: ShouldStartWizardUseCase
) : ActionProcessor<Main.State, Main.Action.RequestWizardStart, Main.Effect> {
	override suspend fun process(
		action: Main.Action.RequestWizardStart,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return shouldStartWizardUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> suspend { state ->
						val content = state as? Main.State.Content
						if (content == null) {
							state
						} else if (!useCaseState.value || content.wizardStartRequested) {
							content
						} else {
							sideEffect(Main.Effect.NavigateToWizard)
							content.copy(wizardStartRequested = true)
						}
					}

					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> suspend { state -> state }
				}
			}
	}
}
