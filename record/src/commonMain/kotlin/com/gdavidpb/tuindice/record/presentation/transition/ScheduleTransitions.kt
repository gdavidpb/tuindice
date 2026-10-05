package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine

internal fun MachineDefinitionBuilder<Schedule.State>.scheduleTransitions(
	machine: ScheduleMachine,
	host: MachineHost<Schedule.Effect>
) {
	// The route's startup can be processed before the initial action, so the observation row
	// lives in fromAny (see the startup-ordering lesson in the doctrine).
	fromAny {
		on<Schedule.Action.ObserveSchedule> { state, _ ->
			machine.startObservation(host = host)
			state
		}

		onTo<ScheduleInternalEvent.ScheduleContentObserved, Schedule.State.Content> { _, event ->
			Schedule.State.Content(
				termName = event.termName,
				schedule = event.schedule,
				viewMode = event.viewMode
			)
		}

		onTo<ScheduleInternalEvent.ScheduleEmptyObserved, Schedule.State.Empty> { _, _ ->
			Schedule.State.Empty
		}

		onTo<ScheduleInternalEvent.ScheduleWaitingObserved, Schedule.State.Loading> { _, _ ->
			Schedule.State.Loading
		}
	}

	// Choosing a view only makes sense with a schedule on screen: the row shows it right away and
	// persists it, so the observation that follows already agrees.
	from<Schedule.State.Content> {
		on<Schedule.Action.SelectScheduleView> { state, action ->
			machine.setViewMode(host = host, viewMode = action.viewMode)
			state.copy(viewMode = action.viewMode)
		}
	}
}
