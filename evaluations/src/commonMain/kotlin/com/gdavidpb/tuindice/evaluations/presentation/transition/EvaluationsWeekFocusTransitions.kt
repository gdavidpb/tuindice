package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.mapper.evaluationsWeekKeyContaining

/**
 * The request to show a week. Only content can show one, so only it applies the request. The states
 * that settle without weeks (empty, no attempts, record data unavailable) drop it, because nothing
 * is going to show it and a request left pending would move the tab whenever content came back much
 * later. The states that are still on their way (idle, loading, failed) leave it pending: the
 * observation sends it again as soon as something settles.
 *
 * Content selects the week the list files the date under, held to the term's weeks like the list,
 * and does not save it as the user's pick.
 */
internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsWeekFocusTransitions(
	machine: EvaluationsMachine
) {
	from<Evaluations.State.Content> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			val isTaken = machine.takeWeekFocus(weekStart = event.weekStart)
			val focusedWeekKey = if (isTaken) {
				evaluationsWeekKeyContaining(weekOneStart = state.weekOneStart, date = event.weekStart)
			} else {
				null
			}
			val focusedWeekItem = state.weekItems.firstOrNull { item -> item.key == focusedWeekKey }

			// Without weeks to show (no anchor) the request was taken all the same: it is not left
			// pending, and the selection stays where it was. A jump is navigation, not a preference:
			// the week is selected for this visit and not saved, so the tab keeps following the
			// current week the next time it opens.
			if (focusedWeekItem == null) {
				state
			} else {
				state.copy(
					selectedWeekKey = focusedWeekItem.key,
					weekItem = focusedWeekItem
				)
			}
		}
	}

	from<Evaluations.State.Empty> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			machine.takeWeekFocus(weekStart = event.weekStart)
			state
		}
	}

	from<Evaluations.State.NoAttempts> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			machine.takeWeekFocus(weekStart = event.weekStart)
			state
		}
	}

	from<Evaluations.State.RecordDataUnavailable> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			machine.takeWeekFocus(weekStart = event.weekStart)
			state
		}
	}

	from<Evaluations.State.Idle> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, _ -> state }
	}

	from<Evaluations.State.Loading> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, _ -> state }
	}

	from<Evaluations.State.Failed> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, _ -> state }
	}
}
