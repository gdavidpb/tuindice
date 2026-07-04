package com.gdavidpb.tuindice.sampleflow.presentation.machine

import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class SampleFlowMachine(
	private val publish: (Any) -> Unit,
	private val eventPublisher: EventPublisher,
	private val host: Any
) {
	private val filter = MutableStateFlow("")

	suspend fun loadSample() {
		withContext(NonCancellable) {
			publish(AppEvent.Action(source = "sampleflow", action = "load"))
			println("sampleflow loaded")
		}
	}

	fun initialState(): Any = Unit
}
