package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.mapper.evaluationsWeekKeyContaining

/**
 * The request to show a week, and the evaluation it may bring into view. Only content can show one, so
 * only it applies the request. The states that settle without weeks (empty, no attempts, record data
 * unavailable) drop it, because nothing is going to show it and a request left pending would move the
 * tab whenever content came back much later. The states that are still on their way (idle, loading,
 * failed) leave it pending: the observation sends it again as soon as something settles.
 *
 * Content selects the week the list files the date under, held to the term's weeks like the list,
 * and does not save it as the user's pick. The evaluation is kept for the list only when the week
 * lists it: a jump to an evaluation that is not there still opens its week.
 */
internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsWeekFocusTransitions(
	machine: EvaluationsMachine
) {
	from<Evaluations.State.Content> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			val request = event.request
			val isTaken = machine.weekFocus.take(request = request)
			val focusedWeekKey = if (isTaken) {
				evaluationsWeekKeyContaining(weekOneStart = state.weekOneStart, date = request.weekStart)
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
				val focusedWeekGroup = state.evaluationWeekGroups.firstOrNull { group -> group.key == focusedWeekItem.key }
				val arrivalEvaluationId = request.evaluationId?.takeIf { evaluationId ->
					focusedWeekGroup?.groups.orEmpty().any { group ->
						group.items.any { item -> item.evaluationId == evaluationId }
					}
				}

				state.copy(
					selectedWeekKey = focusedWeekItem.key,
					weekItem = focusedWeekItem,
					arrivalEvaluationId = arrivalEvaluationId
				)
			}
		}
	}

	from<Evaluations.State.Empty> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			machine.weekFocus.take(request = event.request)
			state
		}
	}

	from<Evaluations.State.NoAttempts> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			machine.weekFocus.take(request = event.request)
			state
		}
	}

	from<Evaluations.State.RecordDataUnavailable> {
		on<EvaluationsInternalEvent.WeekFocusRequested> { state, event ->
			machine.weekFocus.take(request = event.request)
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
