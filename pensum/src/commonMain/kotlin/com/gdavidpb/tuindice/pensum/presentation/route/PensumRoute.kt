package com.gdavidpb.tuindice.pensum.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.ui.screen.PensumScreen

@Composable
fun PensumRoute(
	topBarActionBus: PensumTopBarActionBus,
	onNavigateToSubjectDetail: (subjectCode: String) -> Unit,
	viewModel: PensumViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val showSelectionSheet = remember { mutableStateOf(false) }

	LaunchedEffect(topBarActionBus) {
		topBarActionBus.actions.collect { action ->
			if (action is TopBarAction.ChangePensumAction) {
				showSelectionSheet.value = true
			}
		}
	}

	PensumScreen(
		state = viewState,
		onRetryClick = viewModel::refreshPensumAction,
		showSelectionSheet = showSelectionSheet.value,
		onSummaryCollapsedToggle = viewModel::toggleSummaryCollapsedAction,
		onSelectionSheetDismiss = {
			showSelectionSheet.value = false
		},
		onSubjectStatsClick = onNavigateToSubjectDetail,
		onSelectionApplied = { item, modality ->
			viewModel.selectSelectionAction(
				year = item.year,
				modalityId = modality.id
			)
		},
		onPensumContextClick = {
			showSelectionSheet.value = true
		}
	)
}
