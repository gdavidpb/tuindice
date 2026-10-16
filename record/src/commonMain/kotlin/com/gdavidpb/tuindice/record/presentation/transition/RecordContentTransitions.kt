package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.mapper.hasScheduleOnTerm
import com.gdavidpb.tuindice.record.presentation.mapper.isCurrentTerm
import com.gdavidpb.tuindice.record.presentation.mapper.isSyntheticTerm

internal fun MachineDefinitionBuilder<Record.State>.recordContentTransitions(
	machine: RecordMachine,
	host: MachineHost<Record.Effect>
) {
	from<Record.State.Content> {
		// Selecting a term only makes sense with content on screen: the row reads the
		// view mode from S instead of trusting a payload copy, and any other state
		// rejects the action with telemetry.
		// The screen follows the choice at once, as the store will: the observation that follows brings the same
		// term, and one that comes before it keeps this choice (see the observation of the content).
		on<Record.Action.SelectTerm> { state, action ->
			machine.selectTerm(
				host = host,
				termId = action.termId,
				viewMode = state.viewMode
			)
			state.copy(
				selectedTermId = action.termId,
				hasSelectedTermSchedule = state.record.hasScheduleOnTerm(termId = action.termId),
				isSelectedTermCurrent = state.record.isCurrentTerm(termId = action.termId),
				isSelectedTermSynthetic = state.record.isSyntheticTerm(termId = action.termId)
			)
		}

		// The list has taken the attempt a jump brought in. A later one (another jump) is not the one taken, so
		// the state keeps it.
		on<Record.Action.AttemptArrivalTaken> { state, action ->
			if (state.arrivalAttemptId == action.attemptId) {
				state.copy(arrivalAttemptId = null)
			} else {
				state
			}
		}

		on<RecordInternalEvent.RecordObservationFailed> { state, _ -> state }

		on<RecordInternalEvent.RecordRefreshStarted> { state, _ -> state }

		// A failed refresh keeps the on-screen record; the user still gets told,
		// mirroring the Summary refresh-failure feedback.
		on<RecordInternalEvent.RecordRefreshFailed>(
			emits = setOf(
				Record.Effect.NavigateToOutdatedCredentials::class,
				Record.Effect.ShowSnackBar::class
			)
		) { state, event ->
			if (event.navigateToOutdatedCredentials) {
				host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)
			} else {
				host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))
			}

			state
		}
	}
}
