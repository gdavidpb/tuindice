package com.gdavidpb.tuindice.wizard.presentation.machine

/**
 * Internal machine inputs for the wizard: the completion persisted by the use case
 * re-enters the table to emit the finish effect.
 */
sealed interface WizardInternalEvent {
	data object WizardCompleted : WizardInternalEvent
}
