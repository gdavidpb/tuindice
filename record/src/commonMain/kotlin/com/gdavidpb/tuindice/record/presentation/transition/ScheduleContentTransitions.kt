package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine

internal fun MachineDefinitionBuilder<Schedule.State>.scheduleContentTransitions(
	machine: ScheduleMachine,
	host: MachineHost<Schedule.Effect>
) {
	// Choosing a view only makes sense with a schedule on screen: the row shows it right away and
	// persists it, so the observation that follows already agrees.
	from<Schedule.State.Content> {
		on<Schedule.Action.SelectScheduleView> { state, action ->
			machine.setViewMode(host = host, viewMode = action.viewMode)
			state.copy(viewMode = action.viewMode)
		}
	}
}
