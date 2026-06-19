package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen

@Composable
fun RecordRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToCreateSyntheticTerm: () -> Unit,
	onNavigateToUpdateSyntheticTerm: (termId: String) -> Unit,
	onNavigateToDeleteSyntheticTermConfirmation: (termId: String) -> Unit,
	onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onTopBarTermSelectionAvailable: ((() -> Unit)?) -> Unit,
	onTopBarEnrollmentProofAvailable: ((() -> Unit)?) -> Unit,
	onNavigateToEnrollmentProof: () -> Unit,
	showTopBarBanner: (behavior: TopBarBannerBehavior) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: RecordViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val selectedTermId = (viewState as? Record.State.Content)?.selectedTermId
	val pendingTopBarBanner = remember {
		mutableStateOf<Record.Effect.ShowTopBarBanner?>(null)
	}
	val showTermSelection = remember { mutableStateOf(false) }

	DisposableEffect(viewModel) {
		onTopBarViewModeChangeAvailable(viewModel::setViewModeAction)
		onTopBarTermSelectionAvailable {
			showTermSelection.value = true
		}
		onTopBarEnrollmentProofAvailable(viewModel::openEnrollmentProofAction)

		onDispose {
			onTopBarViewModeChangeAvailable(null)
			onTopBarTermSelectionAvailable(null)
			onTopBarEnrollmentProofAvailable(null)
		}
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Record.Effect.NavigateToOutdatedCredentials ->
				onNavigateToUpdatePassword()

			is Record.Effect.NavigateToEnrollmentProof ->
				onNavigateToEnrollmentProof()

			is Record.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))

			is Record.Effect.ShowTopBarBanner ->
				pendingTopBarBanner.value = effect
		}
	}

	LaunchedEffect(Unit) {
		viewModel.refreshRecordAction()
	}

	LaunchedEffect(viewState, pendingTopBarBanner.value) {
		val currentBanner = pendingTopBarBanner.value ?: return@LaunchedEffect
		val currentViewMode = (viewState as? Record.State.Content)?.viewMode ?: return@LaunchedEffect

		if (currentViewMode != currentBanner.viewMode) return@LaunchedEffect

		showTopBarBanner(currentBanner.behavior)
		pendingTopBarBanner.value = null
	}

	RecordScreen(
		state = viewState,
		selectedTermId = selectedTermId,
		onSelectedTermChange = { termId ->
			if (termId != selectedTermId) {
				viewModel.selectTermAction(termId)
			}
		},
		onRetryClick = viewModel::refreshRecordAction,
		onAttemptSelectionChange = { attemptId, grade, outcome, isSelected ->
			viewModel.upsertAttemptSelectionAction(
				attemptId = attemptId,
				grade = grade,
				outcome = outcome,
				commit = isSelected
			)
		},
		onCreateSyntheticTermClick = onNavigateToCreateSyntheticTerm,
		onUpdateSyntheticTermClick = onNavigateToUpdateSyntheticTerm,
		onDeleteSyntheticTermClick = onNavigateToDeleteSyntheticTermConfirmation,
		showTermSelection = showTermSelection.value,
		onDismissTermSelection = {
			showTermSelection.value = false
		}
	)
}

internal fun Record.State.toRouteViewState(): ViewState {
	return RecordRouteViewState(
		topBarTitle = topBarTitle,
		topBarConfig = when (this) {
			is Record.State.Content -> topBarConfig
			Record.State.Idle,
			Record.State.Empty,
			Record.State.Failed,
			Record.State.Loading,
			-> null
		},
		isTopBarVisible = isTopBarVisible,
		isBottomBarVisible = isBottomBarVisible,
		topBarViewModeState = when (this) {
			is Record.State.Content ->
				RecordTopBarViewModeState(selectedMode = viewMode)

			Record.State.Idle,
			Record.State.Empty,
			Record.State.Failed,
			Record.State.Loading,
			-> null
		}
	)
}
