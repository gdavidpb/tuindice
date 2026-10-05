package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.EmptyView
import com.gdavidpb.tuindice.base.ui.view.LoadingView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.view.ScheduleTableView
import com.gdavidpb.tuindice.record.ui.view.ScheduleViewSwitchView
import com.gdavidpb.tuindice.record.ui.view.ScheduleWeekView
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_empty_message
import tuindice.record.generated.resources.schedule_empty_title

/** The class schedule of the current term, as a table or as the weekly grid. */
@Composable
fun ScheduleScreen(
	state: Schedule.State,
	onViewModeSelected: (ScheduleViewMode) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.testTag(RecordUiTags.ScheduleScreen)
	) {
		SealedCrossfade(
			targetState = state
		) { targetState ->
			when (targetState) {
				is Schedule.State.Idle -> Unit

				is Schedule.State.Loading ->
					LoadingView(indicatorTag = RecordUiTags.ScheduleLoadingIndicator)

				is Schedule.State.Content ->
					ScheduleContentView(
						state = targetState,
						onViewModeSelected = onViewModeSelected
					)

				is Schedule.State.Empty ->
					EmptyView(
						title = stringResource(Res.string.schedule_empty_title),
						message = stringResource(Res.string.schedule_empty_message)
					)
			}
		}
	}
}

@Composable
private fun ScheduleContentView(
	state: Schedule.State.Content,
	onViewModeSelected: (ScheduleViewMode) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(
				start = TuIndiceSpacing.Screen,
				top = InternalScreenDefaults.TopBarSpacing,
				end = TuIndiceSpacing.Screen
			),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			text = state.termName,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		ScheduleViewSwitchView(
			selectedMode = state.viewMode,
			onModeSelected = onViewModeSelected
		)

		// Both views read the same layout; only the one chosen is composed.
		when (state.viewMode) {
			ScheduleViewMode.Table ->
				ScheduleTableView(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					table = state.schedule.table
				)

			ScheduleViewMode.Week ->
				ScheduleWeekView(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					grid = state.schedule.grid
				)
		}
	}
}
