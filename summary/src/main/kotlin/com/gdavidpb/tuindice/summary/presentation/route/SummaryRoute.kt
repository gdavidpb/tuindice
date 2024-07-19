package com.gdavidpb.tuindice.summary.presentation.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SummaryRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SummaryViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	val pickVisualMediaRequest = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia(),
		onResult = { uri -> if (uri != null) viewModel.uploadProfilePictureAction(path = "$uri") }
	)

	val registerTakePicture = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.TakePicture(),
		onResult = { result -> if (result) viewModel.uploadTakenProfilePictureAction() }
	)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Summary.Effect.OpenCamera -> {
				registerTakePicture.launch(Uri.parse(effect.output))

				viewModel.setCameraOutput(effect.output)
			}

			is Summary.Effect.OpenPicker ->
				pickVisualMediaRequest.launch(
					PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
				)

			is Summary.Effect.NavigateToOutdatedPassword ->
				onNavigateToUpdatePassword()

			is Summary.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))

			is Summary.Effect.NavigateToProfilePictureSettingsDialog ->
				onNavigateToProfilePictureSettingsDialog(
					effect.showRemove
				)

			is Summary.Effect.NavigateToRemoveProfilePictureConfirmationDialog ->
				onNavigateToRemoveProfilePictureConfirmationDialog()
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadSummaryAction()
	}

	SummaryScreen(
		state = viewState,
		onRetryClick = viewModel::loadSummaryAction,
		onEditProfilePictureClick = viewModel::openProfilePictureSettingsAction
	)
}