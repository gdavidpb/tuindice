package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_unscheduled

/** The "Horario" view of the current term: the grid and the subjects still to be scheduled. */
@Composable
fun TermScheduleView(
	modifier: Modifier = Modifier,
	grid: ScheduleGridItem,
	onScrollInProgressChange: (Boolean) -> Unit = {}
) {
	val scrollState = rememberScrollState()

	LaunchedEffect(scrollState) {
		snapshotFlow { scrollState.isScrollInProgress }
			.distinctUntilChanged()
			.collect(onScrollInProgressChange)
	}

	DisposableEffect(Unit) {
		onDispose {
			onScrollInProgressChange(false)
		}
	}

	Column(
		modifier = modifier
			.testTag(RecordUiTags.ScheduleContainer)
			.verticalScroll(scrollState)
			.padding(
				start = TuIndiceSpacing.Screen,
				top = TuIndiceSpacing.Medium,
				end = TuIndiceSpacing.Screen,
				bottom = ScheduleBottomPadding
			),
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

private val ScheduleBottomPadding = 120.dp
