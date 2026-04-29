package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinishWizardActionProcessor(
	private val completeWizardUseCase: CompleteWizardUseCase
) : ActionProcessor<Wizard.State, Wizard.Action.Finish, Wizard.Effect>() {
	override suspend fun process(
		action: Wizard.Action.Finish,
		sideEffect: (Wizard.Effect) -> Unit
	): Flow<Mutation<Wizard.State>> {
		return completeWizardUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> suspend { state ->
						sideEffect(Wizard.Effect.FinishWizard)
						state
					}

					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> suspend { state -> state }
				}
			}
	}
}
