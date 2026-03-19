package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SummaryRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SummaryViewModel,
	syncStatusRepository: SyncStatusRepository = koinInject()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val syncStatus by syncStatusRepository
		.observeSyncStatus()
		.collectAsStateWithLifecycle(initialValue = SyncStatus.Healthy)
	val coroutineScope = rememberCoroutineScope()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Summary.Effect.OpenCamera ->
				coroutineScope.launch {
					runCatching { FileKit.openCameraPicker() }
						.getOrNull()
						?.let(viewModel::uploadProfilePictureAction)
				}

			is Summary.Effect.OpenPicker ->
				coroutineScope.launch {
					runCatching { FileKit.openFilePicker(type = FileKitType.Image) }
						.getOrNull()
						?.let(viewModel::uploadProfilePictureAction)
				}

			is Summary.Effect.NavigateToOutdatedCredentials ->
				onNavigateToUpdatePassword()

			is Summary.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))

			is Summary.Effect.ShowProfilePictureSettingsDialog ->
				onNavigateToProfilePictureSettingsDialog(effect.showRemove)

			is Summary.Effect.ShowRemoveProfilePictureConfirmationDialog ->
				onNavigateToRemoveProfilePictureConfirmationDialog()
		}
	}

	SummaryScreen(
		state = viewState,
		syncStatus = syncStatus,
		onRetryClick = viewModel::loadSummaryAction,
		onEditProfilePictureClick = viewModel::openProfilePictureSettingsAction,
		onUpdatePasswordClick = onNavigateToUpdatePassword
	)
}
