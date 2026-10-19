package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Machine host that records every capability call instead of routing it to a ViewModel:
 * effects and internal events are captured for assertions, and machine jobs run in the
 * provided scope (use runTest's backgroundScope so they are cancelled with the test).
 */
class RecordingMachineHost<E : ViewEffect>(
	private val coroutineScope: CoroutineScope
) : MachineHost<E> {
	val sentEffects = mutableListOf<E>()
	val producedInternalEvents = mutableListOf<Any>()

	override fun sendEffect(effect: E) {
		sentEffects += effect
	}

	override fun processInternalEvent(event: Any) {
		producedInternalEvents += event
	}

	override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
		return coroutineScope.launch { block() }
	}
}
