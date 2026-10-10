package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.model.ScheduleTableDefaults

/**
 * The head of the schedule table: an empty cell over the subject column, then the day names over
 * their columns, with today's highlighted.
 */
@Composable
fun ScheduleTableHeaderRow(
	modifier: Modifier = Modifier,
	days: List<ScheduleDay>,
	today: ScheduleDay? = null
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = ScheduleTableDefaults.RowPadding),
		horizontalArrangement = Arrangement.spacedBy(ScheduleTableDefaults.CellGap)
	) {
		Spacer(modifier = Modifier.weight(1f))

		days.forEach { day ->
			ScheduleDayHeaderView(
				modifier = Modifier.width(ScheduleTableDefaults.DayWidth),
				day = day,
				isToday = day == today
			)
		}
	}
}
