package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.ui.dialog.ScheduleContentDialog

@Composable
fun ScheduleRoute(
	viewModel: ScheduleViewModel,
	onDismissRequest: () -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	ScheduleContentDialog(
		state = viewState,
		onViewModeSelected = viewModel::selectScheduleViewAction,
		onDismissRequest = onDismissRequest
	)
}
