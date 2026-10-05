package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_unscheduled

/** The "Semana" view of the schedule: the weekly grid and the subjects still to be scheduled. */
@Composable
fun ScheduleWeekView(
	modifier: Modifier = Modifier,
	grid: ScheduleGridItem
) {
	Column(
		modifier = modifier
			.testTag(RecordUiTags.ScheduleContainer)
			.verticalScroll(rememberScrollState())
			.padding(bottom = TuIndiceSpacing.Screen),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		ScheduleGridView(grid = grid)

		if (grid.unscheduledCodes.isNotEmpty()) {
			Text(
				modifier = Modifier.testTag(RecordUiTags.ScheduleUnscheduled),
				text = stringResource(
					Res.string.schedule_unscheduled,
					grid.unscheduledCodes.joinToString(separator = ", ")
				),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 3,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
