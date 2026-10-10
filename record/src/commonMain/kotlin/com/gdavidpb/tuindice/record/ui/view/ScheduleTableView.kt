package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleTableDefaults

// The narrowest the subject column can get and still fit a code. Five days fit a phone; with a
// weekend day the table grows past the screen and scrolls sideways instead of squeezing the code.
private val MinSubjectWidth = 88.dp

/**
 * The schedule as the university's enrollment proof prints it: one row per subject and one column
 * per day, each meeting reduced to the range of blocks it covers.
 */
@Composable
fun ScheduleTableView(
	modifier: Modifier = Modifier,
	table: ScheduleTableItem
) {
	BoxWithConstraints(modifier = modifier.testTag(RecordUiTags.ScheduleTable)) {
		val daysWidth = (ScheduleTableDefaults.DayWidth + ScheduleTableDefaults.CellGap) * table.days.size
		val minWidth = MinSubjectWidth + daysWidth + ScheduleTableDefaults.RowPadding * 2
		val tableWidth = maxOf(maxWidth, minWidth)

		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.horizontalScroll(rememberScrollState())
				.width(tableWidth)
				.padding(bottom = TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
		) {
			ScheduleTableHeaderRow(days = table.days, today = table.today)

			table.rows.forEach { row ->
				ScheduleTableRowView(
					modifier = Modifier.fillMaxWidth(),
					row = row,
					days = table.days
				)
			}
		}
	}
}
