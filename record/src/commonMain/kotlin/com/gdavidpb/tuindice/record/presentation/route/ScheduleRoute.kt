package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.ui.screen.ScheduleScreen

@Composable
fun ScheduleRoute(
	viewModel: ScheduleViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	ScheduleScreen(
		state = viewState,
		onViewModeSelected = viewModel::selectScheduleViewAction
	)
}
