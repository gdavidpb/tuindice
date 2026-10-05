package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleTableDefaults
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_table_unscheduled

/** "Sin horario": the subject has no meeting on any day, so the message takes all of their columns. */
@Composable
fun ScheduleTableUnscheduledView(
	modifier: Modifier = Modifier,
	attemptId: String,
	dayCount: Int
) {
	val daysWidth = (ScheduleTableDefaults.DayWidth + ScheduleTableDefaults.CellGap) * dayCount -
		ScheduleTableDefaults.CellGap

	Box(
		modifier = modifier
			.width(daysWidth)
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
