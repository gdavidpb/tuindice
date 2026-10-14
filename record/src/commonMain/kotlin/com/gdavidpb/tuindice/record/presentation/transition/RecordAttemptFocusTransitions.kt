package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.mapper.hasScheduleOnTerm

/**
 * The request to show an attempt, and the term that holds it. Only content can show one, so only it
 * applies the request. The state that settles without attempts (empty) drops it, because nothing is going
 * to show it and a request left pending would bring the attempt back whenever content came much later.
 * The states that are still on their way (idle, loading, failed) leave it pending: the observation sends
 * it again as soon as something settles.
 *
 * Content selects the term that holds the attempt, among the ones its view mode shows, and does not save
 * it as the user's pick: a jump is navigation, and the next visit follows the saved term again. An attempt
 * the view mode does not show, or the record does not have, only opens the tab.
 */
internal fun MachineDefinitionBuilder<Record.State>.recordAttemptFocusTransitions(
	machine: RecordMachine
) {
	from<Record.State.Content> {
		on<RecordInternalEvent.AttemptFocusRequested> { state, event ->
			val isTaken = machine.attemptFocus.take(request = event.request)
			val term = if (isTaken) state.termHolding(attemptId = event.request.attemptId) else null

			if (term == null) {
				state
			} else {
				state.copy(
					selectedTermId = term.id,
					hasSelectedTermSchedule = state.record.hasScheduleOnTerm(termId = term.id),
					arrivalAttemptId = event.request.attemptId
				)
			}
		}
	}

	from<Record.State.Empty> {
		on<RecordInternalEvent.AttemptFocusRequested> { state, event ->
			machine.attemptFocus.take(request = event.request)
			state
		}
	}

	from<Record.State.Idle> {
		on<RecordInternalEvent.AttemptFocusRequested> { state, _ -> state }
	}

	from<Record.State.Loading> {
		on<RecordInternalEvent.AttemptFocusRequested> { state, _ -> state }
	}

	from<Record.State.Failed> {
		on<RecordInternalEvent.AttemptFocusRequested> { state, _ -> state }
	}
}

// The term, among the ones the view mode shows, that lists the attempt.
private fun Record.State.Content.termHolding(attemptId: String): TermProjection? {
	return record.filteredProjectionFor(viewMode).terms.firstOrNull { term ->
		term.attempts.any { attempt -> attempt.id == attemptId }
	}
}
