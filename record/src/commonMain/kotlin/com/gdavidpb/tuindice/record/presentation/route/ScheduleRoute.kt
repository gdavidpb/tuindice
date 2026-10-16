package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.ResumedEffect
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.ui.dialog.ScheduleContentDialog
import kotlinx.datetime.LocalDate

@Composable
fun ScheduleRoute(
	viewModel: ScheduleViewModel,
	onDismissRequest: () -> Unit,
	// What a cell with an evaluation asks for: the Evaluations tab on the week that opens on that Monday,
	// on that evaluation. The host of the sheet has to take the student there.
	onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	// The clock sleeps with the process: reading it again is the first thing coming back does.
	ResumedEffect(action = viewModel::resumedAction)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Schedule.Effect.NavigateToEvaluationsWeek ->
				onNavigateToEvaluationsWeek(effect.weekStart, effect.evaluationId)
		}
	}

	ScheduleContentDialog(
		state = viewState,
		onViewModeSelected = viewModel::selectScheduleViewAction,
		onEvaluationClick = viewModel::evaluationCellSelectedAction,
		onDismissRequest = onDismissRequest
	)
}
