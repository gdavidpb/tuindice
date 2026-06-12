package com.gdavidpb.tuindice.wizard.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.wizard.presentation.contract.CURRENT_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.HISTORICAL_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.machine.WizardInternalEvent
import com.gdavidpb.tuindice.wizard.presentation.machine.WizardMachine
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId

internal fun MachineDefinitionBuilder<Wizard.State>.wizardTransitions(
	machine: WizardMachine,
	host: MachineHost<Wizard.Effect>
) {
	from<Wizard.State.Content> {
		on<Wizard.Action.Advance> { state, _ -> state.advance() }

		on<Wizard.Action.Back> { state, _ -> state.goBack() }

		on<Wizard.Action.OpenSubjectDetail> { state, _ ->
			state.goTo(WizardStepId.SubjectDetail)
		}

		on<Wizard.Action.OpenEvaluationForm> { state, _ ->
			state.goTo(WizardStepId.EvaluationForm)
		}

		// Top bar actions are consumed inside the wizard so they never trigger real
		// app side effects.
		on<Wizard.Action.ConsumeTopBarAction> { state, action ->
			when (action.topBarAction) {
				TopBarAction.RecordTermSelectionAction ->
					if (state.currentStep.id.isRecordStep) {
						state.copy(isRecordTermSelectionVisible = true)
					} else {
						state
					}

				else -> state
			}
		}

		on<Wizard.Action.DismissRecordTermSelection> { state, _ ->
			state.copy(isRecordTermSelectionVisible = false)
		}

		on<Wizard.Action.SelectSubjectTab> { state, action ->
			state.copy(selectedSubjectTab = action.tab)
		}

		on<Wizard.Action.SelectTerm> { state, action ->
			state.copy(
				selectedTermId = action.termId,
				isRecordTermSelectionVisible = false
			)
		}

		on<Wizard.Action.SetRecordViewMode> { state, action ->
			state.copy(
				recordViewMode = action.viewMode,
				selectedTermId = when (action.viewMode) {
					RecordViewMode.Historical -> HISTORICAL_TERM_ID
					RecordViewMode.Projection -> CURRENT_TERM_ID
				}
			)
		}

		on<Wizard.Action.SetSubjectChartsVisible> { state, action ->
			when {
				action.isVisible && state.currentStep.id == WizardStepId.SubjectDetail ->
					state.goTo(WizardStepId.SubjectCharts)

				!action.isVisible && state.currentStep.id == WizardStepId.SubjectCharts ->
					state.goTo(WizardStepId.SubjectDetail)

				else -> state
			}
		}

		on<Wizard.Action.Dismiss> { state, _ ->
			machine.complete(host = host)
			state
		}

		on<Wizard.Action.Finish> { state, _ ->
			machine.complete(host = host)
			state
		}

		on<WizardInternalEvent.WizardCompleted>(
			emits = setOf(Wizard.Effect.FinishWizard::class)
		) { state, _ ->
			host.sendEffect(Wizard.Effect.FinishWizard)
			state
		}
	}
}

private val WizardStepId.isRecordStep: Boolean
	get() = this == WizardStepId.Record || this == WizardStepId.RecordActions
