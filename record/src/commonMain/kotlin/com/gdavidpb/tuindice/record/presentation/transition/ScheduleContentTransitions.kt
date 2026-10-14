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

		// Only a cell with an evaluation can be tapped, in the table or on the grid, and it only asks to be
		// taken there: the screen stays as it is until whoever hosts the sheet answers.
		on<Schedule.Action.EvaluationCellSelected>(
			emits = setOf(Schedule.Effect.NavigateToEvaluationsWeek::class)
		) { state, action ->
			host.sendEffect(
				Schedule.Effect.NavigateToEvaluationsWeek(weekStart = action.weekStart, evaluationId = action.evaluationId)
			)
			state
		}
	}
}
