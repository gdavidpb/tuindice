package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import kotlinx.datetime.LocalDate

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
		// taken there: the screen stays as it is until whoever hosts the sheet answers. The week asked for is
		// the one the content has for that mark; an evaluation the content does not mark is not asked for.
		on<Schedule.Action.EvaluationCellSelected>(
			emits = setOf(Schedule.Effect.NavigateToEvaluationsWeek::class)
		) { state, action ->
			val weekStart = state.weekStartOfMark(evaluationId = action.evaluationId) ?: return@on state

			host.sendEffect(
				Schedule.Effect.NavigateToEvaluationsWeek(weekStart = weekStart, evaluationId = action.evaluationId)
			)
			state
		}
	}
}

// The Monday of the week that the mark of [evaluationId] is in, read from the cell the content draws the mark
// on; null when no cell of the content carries that evaluation.
private fun Schedule.State.Content.weekStartOfMark(evaluationId: String): LocalDate? {
	return schedule.grid.days
		.flatMap(ScheduleDayItem::cells)
		.firstOrNull { cell -> cell.evaluationId == evaluationId }
		?.evaluationWeekStart
}
