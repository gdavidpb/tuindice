package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toSyncStatusText
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker

@Composable
fun SummaryRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SummaryViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val sync = viewState.sync
	// The state carries the instant and the text is described here: it says "today" or "yesterday"
	// by the day it is rendered, not by the day the sync was observed.
	val syncStatusText = sync.lastSuccessfulSyncAt.toSyncStatusText()
	val screenState = when (val currentState = viewState) {
		is Summary.State.Content -> currentState.copy(syncStatusText = syncStatusText)
		else -> currentState
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Summary.Effect.OpenCamera ->
				runCatching { FileKit.openCameraPicker() }
					.getOrNull()
					?.let(viewModel::uploadProfilePictureAction)

			is Summary.Effect.OpenPicker ->
				runCatching { FileKit.openFilePicker(type = FileKitType.Image) }
					.getOrNull()
					?.let(viewModel::uploadProfilePictureAction)

			is Summary.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))

			is Summary.Effect.ShowProfilePictureSettingsDialog ->
				onNavigateToProfilePictureSettingsDialog(effect.showRemove)

			is Summary.Effect.ShowRemoveProfilePictureConfirmationDialog ->
				onNavigateToRemoveProfilePictureConfirmationDialog()
		}
	}

	LaunchedEffect(Unit) {
		viewModel.refreshSummaryAction()
	}

	SummaryScreen(
		state = screenState,
		syncStatus = sync.status,
		syncReport = sync.report,
		isSyncing = sync.isSyncing,
		onRetryClick = viewModel::refreshSummaryAction,
		onEditProfilePictureClick = viewModel::openProfilePictureSettingsAction,
		onUpdatePasswordClick = onNavigateToUpdatePassword
	)
}
