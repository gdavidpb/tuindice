package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.view.ScheduleTableView
import com.gdavidpb.tuindice.record.ui.view.ScheduleViewSwitchView
import com.gdavidpb.tuindice.record.ui.view.ScheduleWeekView
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.dialog_title_schedule
import tuindice.record.generated.resources.schedule_empty_message

/**
 * The class schedule of the current term, as a table or as the weekly grid. A sheet over the
 * record and not a screen of its own: a handful of subjects is something to glance at without
 * leaving the term being looked at, and the sheet is only as tall as what it holds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContentDialog(
	state: Schedule.State,
	onViewModeSelected: (ScheduleViewMode) -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		// Whole from the start: a long week grid scrolls inside it instead of opening half hidden.
		sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.testTag(RecordUiTags.ScheduleSheet)
				.fillMaxWidth()
				.padding(bottom = TuIndiceSpacing.Dialog),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Section)
		) {
			ScheduleHeaderView(
				content = state as? Schedule.State.Content,
				onViewModeSelected = onViewModeSelected
			)

			when (state) {
				is Schedule.State.Idle -> Unit

				is Schedule.State.Loading ->
					Box(
						modifier = Modifier.fillMaxWidth(),
						contentAlignment = Alignment.Center
					) {
						CircularProgressIndicator(
							modifier = Modifier.testTag(RecordUiTags.ScheduleLoadingIndicator)
						)
					}

				is Schedule.State.Content ->
					ScheduleViewsView(state = state)

				is Schedule.State.Empty ->
					Text(
						text = stringResource(Res.string.schedule_empty_message),
						style = MaterialTheme.typography.bodyLarge
					)
			}
		}
	}
}

// The sheet's title with the term it speaks of right under it, as one block, and the view switch at
// its side once there is a schedule to draw.
@Composable
private fun ScheduleHeaderView(
	content: Schedule.State.Content?,
	onViewModeSelected: (ScheduleViewMode) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Section),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
		) {
			Text(
				modifier = Modifier.testTag(RecordUiTags.ScheduleTitle),
				text = stringResource(Res.string.dialog_title_schedule),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)

			if (content != null) {
				Text(
					text = content.termName,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}

		if (content != null) {
			ScheduleViewSwitchView(
				selectedMode = content.viewMode,
				onModeSelected = onViewModeSelected
			)
		}
	}
}

@Composable
private fun ScheduleViewsView(state: Schedule.State.Content) {
	// Both views read the same layout; only the one chosen is composed.
	when (state.viewMode) {
		ScheduleViewMode.Table ->
			ScheduleTableView(
				modifier = Modifier.fillMaxWidth(),
				table = state.schedule.table
			)

		ScheduleViewMode.Week ->
			ScheduleWeekView(
				modifier = Modifier.fillMaxWidth(),
				grid = state.schedule.grid
			)
	}
}
