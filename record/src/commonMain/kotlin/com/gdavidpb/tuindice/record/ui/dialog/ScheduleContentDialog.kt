package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.view.ScheduleHeaderView
import com.gdavidpb.tuindice.record.ui.view.ScheduleViewsView
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
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
