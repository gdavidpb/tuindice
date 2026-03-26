package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen

@Composable
fun RecordRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: RecordViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val selectedQuarterIdState = remember {
		mutableStateOf<String?>(null)
	}

	LaunchedEffect(viewState) {
		selectedQuarterIdState.value = when (viewState) {
			is Record.State.Content -> {
				val contentState = viewState as Record.State.Content
				val quarters = contentState.quarters

				when {
					quarters.isEmpty() -> null
					quarters.any { quarter -> quarter.id == selectedQuarterIdState.value } ->
						selectedQuarterIdState.value
					else -> quarters.first().id
				}
			}

			Record.State.Empty,
			Record.State.Failed,
			Record.State.Loading,
			-> null
		}
	}

	LaunchedEffect(viewState, selectedQuarterIdState.value) {
		onViewStateChanged(
			viewState.toRouteViewState(
				selectedQuarterId = selectedQuarterIdState.value
			)
		)
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
		selectedQuarterId = selectedQuarterIdState.value,
		onSelectedQuarterChange = { quarterId ->
			selectedQuarterIdState.value = quarterId
		},
		onRetryClick = viewModel::refreshQuartersAction,
		onSubjectGradeChange = viewModel::updateSubjectAction
	)
}

private fun Record.State.toRouteViewState(
	selectedQuarterId: String?
): ViewState {
	val derivedTopBarConfig = when (this) {
		is Record.State.Content -> {
			val selectedQuarter = quarters.firstOrNull { quarter ->
				quarter.id == (selectedQuarterId ?: quarters.firstOrNull()?.id)
			}

			if (selectedQuarter?.isCurrent == true) topBarConfig else null
		}

		Record.State.Empty,
		Record.State.Failed,
		Record.State.Loading,
		-> null
	}

	return object : ViewState(
		topBarTitle = topBarTitle,
		topBarConfig = derivedTopBarConfig,
		isTopBarVisible = isTopBarVisible,
		isBottomBarVisible = isBottomBarVisible
	) {}
}
