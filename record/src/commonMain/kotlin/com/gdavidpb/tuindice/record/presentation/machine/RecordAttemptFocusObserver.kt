package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.RecordAttemptFocusRequest
import com.gdavidpb.tuindice.base.domain.repository.RecordAttemptFocusRepository
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull

/**
 * The request to show an attempt, wired into the Record machine. It is observed while the screen lives, so a
 * request made while the content is up reaches the machine at once, and it is sent again each time an observation
 * settles, so one made before the content loaded is applied (or dropped) then. Only the first to take it applies it.
 */
internal class RecordAttemptFocusObserver(
	private val repository: RecordAttemptFocusRepository
) {
	// The collector of the requests: an observation restart must not leave one more feeding the same machine.
	private var observationJob: Job? = null

	fun observe(host: MachineHost<Record.Effect>) {
		if (observationJob?.isActive == true) return

		observationJob = host.launchMachineJob {
			repository.pending.filterNotNull().collect { request ->
				host.processInternalEvent(RecordInternalEvent.AttemptFocusRequested(request = request))
			}
		}
	}

	// The request still pending, sent again now that the screen has settled on something that can apply it or drop it.
	fun sendPending(host: MachineHost<Record.Effect>) {
		repository.pending.value?.let { request ->
			host.processInternalEvent(RecordInternalEvent.AttemptFocusRequested(request = request))
		}
	}

	fun take(request: RecordAttemptFocusRequest): Boolean {
		return repository.consume(request = request)
	}
}
