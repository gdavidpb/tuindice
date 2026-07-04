package com.gdavidpb.tuindice.pensum.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.ui.screen.PensumScreen

@Composable
fun PensumRoute(
	topBarActionBus: PensumTopBarActionBus,
	screenSessionStore: PensumScreenSessionStore,
	onNavigateToSubjectDetail: (subjectCode: String) -> Unit,
	viewModel: PensumViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val showSelectionSheet = remember {
		mutableStateOf(screenSessionStore.isSelectionSheetVisible)
	}

	LaunchedEffect(topBarActionBus) {
		topBarActionBus.actions.collect { action ->
			if (action is TopBarAction.ChangePensumAction) {
				showSelectionSheet.value = true
				screenSessionStore.isSelectionSheetVisible = true
			}
		}
	}

	PensumScreen(
		state = viewState,
		onRetryClick = viewModel::refreshPensumAction,
		showSelectionSheet = showSelectionSheet.value,
		screenSessionStore = screenSessionStore,
		onSummaryCollapsedToggle = viewModel::toggleSummaryCollapsedAction,
		onSelectionSheetDismiss = {
			showSelectionSheet.value = false
			screenSessionStore.isSelectionSheetVisible = false
		},
		onSubjectStatsClick = onNavigateToSubjectDetail,
		onSelectionApplied = { item, modality ->
			viewModel.selectSelectionAction(
				year = item.year,
				modalityId = modality.id
			)
			showSelectionSheet.value = false
			screenSessionStore.isSelectionSheetVisible = false
		},
		onPensumContextClick = {
			showSelectionSheet.value = true
			screenSessionStore.isSelectionSheetVisible = true
		}
	)
}
