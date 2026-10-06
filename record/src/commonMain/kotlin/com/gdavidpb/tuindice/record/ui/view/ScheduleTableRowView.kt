package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleTableDefaults

/**
 * One subject of the table: its code and where it is taught on the left, then a cell per day.
 * A meeting that clashes with another subject's is outlined in the alert tone; nothing is written
 * under the row for it.
 */
@Composable
fun ScheduleTableRowView(
	modifier: Modifier = Modifier,
	row: ScheduleTableRowItem,
	days: List<ScheduleDay>
) {
	val description = row.description.asString()

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Surface(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(RecordUiTags.scheduleTableRow(row.attemptId))
				.semantics(mergeDescendants = true) { contentDescription = description },
			shape = RoundedCornerShape(TuIndiceRadius.Small),
			// One step above the sheet the table sits on, so each subject reads as its own row.
			color = MaterialTheme.colorScheme.surfaceContainerHigh
		) {
			Row(
				modifier = Modifier
					.padding(
						horizontal = ScheduleTableDefaults.RowPadding,
						vertical = TuIndiceSpacing.Medium
					),
				horizontalArrangement = Arrangement.spacedBy(ScheduleTableDefaults.CellGap),
				verticalAlignment = Alignment.CenterVertically
			) {
				ScheduleTableSubjectView(
					modifier = Modifier.weight(1f),
					row = row
				)

				if (row.isUnscheduled) {
					ScheduleTableUnscheduledView(
						attemptId = row.attemptId,
						dayCount = days.size
					)
				} else {
					days.forEach { day ->
						ScheduleTableDayCellView(
							modifier = Modifier.width(ScheduleTableDefaults.DayWidth),
							attemptId = row.attemptId,
							day = day,
							meetings = row.meetings[day].orEmpty()
						)
					}
				}
			}
		}
	}
}
