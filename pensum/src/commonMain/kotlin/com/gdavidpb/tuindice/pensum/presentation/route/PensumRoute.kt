package com.gdavidpb.tuindice.pensum.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.ui.screen.PensumScreen

@Composable
fun PensumRoute(
	showSnackBar: (message: SnackBarMessage) -> Unit,
	topBarActionBus: PensumTopBarActionBus,
	onNavigateToSubjectDetail: (subjectCode: String) -> Unit,
	viewModel: PensumViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val showSelectionSheet = remember { mutableStateOf(false) }
	val pendingSnackBarMessage = remember { mutableStateOf<UiText?>(null) }
	val pendingSnackBarText = pendingSnackBarMessage.value?.asString()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Pensum.Effect.ShowSnackBar -> pendingSnackBarMessage.value = effect.message
		}
	}

	if (pendingSnackBarText != null) {
		LaunchedEffect(pendingSnackBarText) {
			showSnackBar(SnackBarMessage(message = pendingSnackBarText))
			pendingSnackBarMessage.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.refreshPensumAction()
	}

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
