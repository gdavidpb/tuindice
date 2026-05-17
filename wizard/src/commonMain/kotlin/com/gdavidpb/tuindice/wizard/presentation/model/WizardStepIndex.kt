package com.gdavidpb.tuindice.wizard.presentation.model

fun List<WizardStep>.indexOfStep(stepId: WizardStepId): Int {
	return indexOfFirst { step -> step.id == stepId }
		.takeIf { index -> index >= 0 }
		?: 0
}
