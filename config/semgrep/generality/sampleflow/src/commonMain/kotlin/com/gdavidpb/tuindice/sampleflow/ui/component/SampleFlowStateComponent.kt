package com.gdavidpb.tuindice.sampleflow.ui.component

// Positivo de ui-component-no-else-unit: el when de component cierra con else -> Unit.
fun sampleFlowStateComponent(state: SampleFlowState): String {
	return when (state) {
		SampleFlowState.Idle -> "idle"
		else -> Unit
	}.toString()
}
