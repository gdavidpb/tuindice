package com.gdavidpb.tuindice.sampleflow.presentation.machine

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class SampleFlowMachine(
	private val publish: (Any) -> Unit
) {
	private val filter = MutableStateFlow("")

	suspend fun loadSample() {
		withContext(NonCancellable) {
			publish(AppEvent.Action(source = "sampleflow", action = "load"))
		}
	}

	fun initialState(): Any = Unit
}
