package com.gdavidpb.tuindice.pensum.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.ui.screen.PensumScreen

@Composable
fun PensumRoute(
	showSnackBar: (message: SnackBarMessage) -> Unit,
	topBarActionBus: PensumTopBarActionBus,
	viewModel: PensumViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val showSelectionSheet = remember { mutableStateOf(false) }

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Pensum.Effect.ShowSnackBar -> showSnackBar(SnackBarMessage(message = effect.message))
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
		onSelectionApplied = { item, modality ->
			viewModel.selectSelectionAction(
				careerCode = item.careerCode,
				year = item.year,
				modalityId = modality.id
			)
		}
	)
}
