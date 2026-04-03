package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
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
	onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: RecordViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val selectedQuarterId = (viewState as? Record.State.Content)?.selectedQuarterId

	LaunchedEffect(viewState) {
		onViewStateChanged(viewState.toRouteViewState())
	}

	DisposableEffect(viewModel) {
		onTopBarViewModeChangeAvailable(viewModel::setViewModeAction)

		onDispose {
			onTopBarViewModeChangeAvailable(null)
		}
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Record.Effect.NavigateToOutdatedCredentials ->
				onNavigateToUpdatePassword()

			is Record.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	LaunchedEffect(Unit) {
		viewModel.refreshQuartersAction()
	}

	RecordScreen(
		state = viewState,
		selectedQuarterId = selectedQuarterId,
		onSelectedQuarterChange = { quarterId ->
			if (quarterId != selectedQuarterId) {
				viewModel.selectQuarterAction(quarterId)
			}
		},
		onRetryClick = viewModel::refreshQuartersAction,
		onSubjectGradeChange = viewModel::updateSubjectAction
	)
}

private fun Record.State.toRouteViewState(): ViewState {
	val isEnrollmentProofVisible = when (this) {
		is Record.State.Content ->
			quarters.any { quarter ->
				quarter.id == selectedQuarterId && quarter.isCurrent
			}

		Record.State.Empty,
		Record.State.Failed,
		Record.State.Loading,
		-> false
	}

	return RecordRouteViewState(
		topBarTitle = topBarTitle,
		topBarConfig = if (isEnrollmentProofVisible) topBarConfig else null,
		isTopBarVisible = isTopBarVisible,
		isBottomBarVisible = isBottomBarVisible,
		topBarViewModeState = when (this) {
			is Record.State.Content ->
				RecordTopBarViewModeState(selectedMode = viewMode)

			Record.State.Empty,
			Record.State.Failed,
			Record.State.Loading,
			-> null
		}
	)
}
