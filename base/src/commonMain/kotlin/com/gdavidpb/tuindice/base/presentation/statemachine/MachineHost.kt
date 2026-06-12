package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * The capabilities a machine's transition outputs may use: emitting effects declared by
 * the active row, feeding internal events back into the FIFO loop, and launching
 * machine-scoped jobs. Provided by [StateMachineViewModel.machineHost].
 */
interface MachineHost<E : ViewEffect> {
	fun sendEffect(effect: E)

	fun processInternalEvent(event: Any)

	fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job
}
