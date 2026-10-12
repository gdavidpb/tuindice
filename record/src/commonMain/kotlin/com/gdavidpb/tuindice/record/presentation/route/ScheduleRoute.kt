package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.ResumedEffect
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.ui.dialog.ScheduleContentDialog

@Composable
fun ScheduleRoute(
	viewModel: ScheduleViewModel,
	onDismissRequest: () -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	// The clock sleeps with the process: reading it again is the first thing coming back does.
	ResumedEffect(action = viewModel::resumedAction)

	ScheduleContentDialog(
		state = viewState,
		onViewModeSelected = viewModel::selectScheduleViewAction,
		onDismissRequest = onDismissRequest
	)
}
