package com.gdavidpb.tuindice.sampleflow.presentation.machine

import kotlinx.coroutines.flow.MutableStateFlow

// Control negativo: los Draft reifican registros de entrada con MutableStateFlow
// y están exentos por paths (machine-state-only-in-draft excluye **/*Draft.kt).
class SampleFlowDraft {
	val name = MutableStateFlow("")
	val code = MutableStateFlow("")
}
