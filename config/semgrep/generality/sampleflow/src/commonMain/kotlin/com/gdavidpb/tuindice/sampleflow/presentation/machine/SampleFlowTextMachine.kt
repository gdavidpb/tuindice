package com.gdavidpb.tuindice.sampleflow.presentation.machine

// Positivo de machine-no-text-resources: la máquina arma el texto con recursos y UiText.
class SampleFlowTextMachine {
	fun label(): String = getString(Res.string.sample_label)

	fun message(): UiText = UiText.Resource(Res.string.sample_label)
}
