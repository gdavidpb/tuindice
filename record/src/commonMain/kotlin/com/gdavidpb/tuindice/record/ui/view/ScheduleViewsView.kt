package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import kotlinx.datetime.LocalDate

/** The schedule drawn the way the student chose. Both views read the same layout; only the one chosen is composed. */
@Composable
fun ScheduleViewsView(
	modifier: Modifier = Modifier,
	state: Schedule.State.Content,
	onEvaluationClick: (weekStart: LocalDate, evaluationId: String) -> Unit
) {
	when (state.viewMode) {
		ScheduleViewMode.Table ->
			ScheduleTableView(
				modifier = modifier.fillMaxWidth(),
				table = state.schedule.table,
				onEvaluationClick = onEvaluationClick
			)

		ScheduleViewMode.Week ->
			ScheduleWeekView(
				modifier = modifier.fillMaxWidth(),
				grid = state.schedule.grid,
				onEvaluationClick = onEvaluationClick
			)
	}
}
