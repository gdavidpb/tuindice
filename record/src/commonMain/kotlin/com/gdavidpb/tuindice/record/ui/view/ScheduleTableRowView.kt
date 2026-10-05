package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_table_section
import tuindice.record.generated.resources.schedule_table_section_classroom
import tuindice.record.generated.resources.schedule_table_unscheduled

/**
 * One subject of the table: its code and where it is taught on the left, then a cell per day.
 * What the university flagged on its enrollment hangs under the row, in the alert tone.
 */
@Composable
fun ScheduleTableRowView(
	modifier: Modifier = Modifier,
	row: ScheduleTableRowItem,
	days: List<ScheduleDay>
) {
	val description = scheduleTableRowDescription(row = row)

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
			color = MaterialTheme.colorScheme.surfaceContainerLow
		) {
			Row(
				modifier = Modifier.padding(
					horizontal = ScheduleTableRowPadding,
					vertical = TuIndiceSpacing.Medium
				),
				horizontalArrangement = Arrangement.spacedBy(ScheduleTableCellGap),
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
							modifier = Modifier.width(ScheduleTableDayWidth),
							attemptId = row.attemptId,
							day = day,
							meetings = row.meetings[day].orEmpty()
						)
					}
				}
			}
		}

		if (row.errorText != null) {
			ScheduleTableErrorView(
				modifier = Modifier.padding(horizontal = ScheduleTableRowPadding),
				attemptId = row.attemptId,
				text = row.errorText
			)
		}
	}
}

// "CI5311" over "Sec. 1 · MYS-116". The classroom stands alone when the section is unknown, and a
// subject with neither is only its code.
@Composable
private fun ScheduleTableSubjectView(
	modifier: Modifier,
	row: ScheduleTableRowItem
) {
	val detail = when {
		row.section != null && row.classroom != null ->
			stringResource(Res.string.schedule_table_section_classroom, row.section, row.classroom)

		row.section != null -> stringResource(Res.string.schedule_table_section, row.section)

		else -> row.classroom
	}

	Column(modifier = modifier) {
		Text(
			text = row.subjectCode,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		if (detail != null) {
			Text(
				text = detail,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

// The subject has no meeting on any day, so the message takes all of their columns.
@Composable
private fun ScheduleTableUnscheduledView(
	attemptId: String,
	dayCount: Int
) {
	Box(
		modifier = Modifier
			.width((ScheduleTableDayWidth + ScheduleTableCellGap) * dayCount - ScheduleTableCellGap)
			.testTag(RecordUiTags.scheduleTableUnscheduled(attemptId)),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = stringResource(Res.string.schedule_table_unscheduled),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}
