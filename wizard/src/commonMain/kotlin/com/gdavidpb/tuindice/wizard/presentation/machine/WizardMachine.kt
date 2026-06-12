package com.gdavidpb.tuindice.wizard.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.transition.wizardTransitions

class WizardMachine(
	private val completeWizardUseCase: CompleteWizardUseCase
) : ScreenMachine<Wizard.State, Wizard.Effect> {
	override fun initialState(): Wizard.State = Wizard.State.Content()

	override fun define(host: MachineHost<Wizard.Effect>): MachineDefinition<Wizard.State> {
		return MachineDefinition.define {
			wizardTransitions(machine = this@WizardMachine, host = host)
		}
	}

	internal fun complete(host: MachineHost<Wizard.Effect>) {
		host.launchMachineJob {
			completeWizardUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> host.processInternalEvent(
						WizardInternalEvent.WizardCompleted
					)

					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> Unit
				}
			}
		}
	}
}
