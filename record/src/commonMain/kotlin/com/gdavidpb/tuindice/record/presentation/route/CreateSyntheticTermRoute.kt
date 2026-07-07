package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.ui.dialog.DiscardSyntheticTermContentDialog
import com.gdavidpb.tuindice.record.ui.screen.CreateSyntheticTermScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateSyntheticTermRoute(
	termId: String?,
	viewModel: CreateSyntheticTermViewModel,
	onBack: () -> Unit,
	onBackInterceptorAvailable: ((() -> Boolean)?) -> Unit,
	onSubjectStatsClick: (String) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val hasDiscardableDraft = rememberUpdatedState(viewState.hasDiscardableDraft)
	val showDiscardDialog = remember { mutableStateOf(false) }

	LaunchedEffect(termId) {
		viewModel.configureAction(termId)
	}

	DisposableEffect(viewModel) {
		onBackInterceptorAvailable {
			if (hasDiscardableDraft.value) {
				showDiscardDialog.value = true
				true
			} else {
				false
			}
		}

		onDispose {
			onBackInterceptorAvailable(null)
		}
	}

	BackHandler(enabled = viewState.hasDiscardableDraft) {
		showDiscardDialog.value = true
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			CreateSyntheticTerm.Effect.NavigateBack ->
				onBack()
		}
	}

	CreateSyntheticTermScreen(
		state = viewState,
		onQueryChange = viewModel::updateQueryAction,
		onClearQueryClick = viewModel::clearQueryAction,
		onPeriodSelected = viewModel::selectPeriodAction,
		onAddSubjectTabSelected = viewModel::selectAddSubjectTabAction,
		onSubjectAdd = viewModel::addSubjectAction,
		onSubjectRemove = viewModel::removeSubjectAction,
		onSubjectStatsClick = onSubjectStatsClick,
		onCreateClick = viewModel::createTermAction
	)

	if (showDiscardDialog.value) {
		DiscardSyntheticTermContentDialog(
			onConfirmClick = {
				showDiscardDialog.value = false
				onBack()
			},
			onDismissRequest = { showDiscardDialog.value = false }
		)
	}
}
