package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/** The "Semana" view of the schedule: the weekly grid and the subjects still to be scheduled. */
@Composable
fun ScheduleWeekView(
	modifier: Modifier = Modifier,
	grid: ScheduleGridItem
) {
	Column(
		modifier = modifier
			.testTag(RecordUiTags.ScheduleContainer)
			.padding(bottom = TuIndiceSpacing.Screen),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		// The grid takes what the sheet leaves and scrolls inside it; the line below stays in sight.
		ScheduleGridView(
			modifier = Modifier.weight(1f, fill = false),
			grid = grid
		)

		if (grid.unscheduledText != null) {
			Text(
				modifier = Modifier.testTag(RecordUiTags.ScheduleUnscheduled),
				text = grid.unscheduledText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 3,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
