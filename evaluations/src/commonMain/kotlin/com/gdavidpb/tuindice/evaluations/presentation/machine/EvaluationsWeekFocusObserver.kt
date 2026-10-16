package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.EvaluationsWeekFocusRequest
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull

/**
 * The request to show a week of the Evaluations tab, wired into the Evaluations machine. It is observed while the
 * screen lives, so a request made while the content is up reaches the machine at once, and it is sent again each time
 * an observation settles, so one made before the content loaded is applied (or dropped) then. Only the first to take
 * it applies it.
 */
internal class EvaluationsWeekFocusObserver(
	private val repository: EvaluationsWeekFocusRepository
) {
	// The collector of the requests: an observation restart must not leave one more feeding the same machine.
	private var observationJob: Job? = null

	fun observe(host: MachineHost<Evaluations.Effect>) {
		if (observationJob?.isActive == true) return

		observationJob = host.launchMachineJob {
			repository.pending.filterNotNull().collect { request ->
				host.processInternalEvent(EvaluationsInternalEvent.WeekFocusRequested(request = request))
			}
		}
	}

	// The request still pending, sent again now that the screen has settled on something that can apply it or drop it.
	fun sendPending(host: MachineHost<Evaluations.Effect>) {
		repository.pending.value?.let { request ->
			host.processInternalEvent(EvaluationsInternalEvent.WeekFocusRequested(request = request))
		}
	}

	/** Takes the pending [request]: true only for the first caller. */
	fun take(request: EvaluationsWeekFocusRequest): Boolean {
		return repository.consume(request = request)
	}
}
