package com.gdavidpb.tuindice.summary.presentation.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.hasCamera
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryDialog
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationDialog
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryRoute(
	onNavigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SummaryViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val sheetState = rememberModalBottomSheetState()
	val dialogState = rememberDialogState<SummaryDialog>()

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

			is Summary.Effect.ShowProfilePictureSettingsDialog ->
				dialogState.value = SummaryDialog.ProfilePictureSettings(
					showTake = context.hasCamera(),
					showRemove = effect.showRemove
				)

			is Summary.Effect.ShowRemoveProfilePictureConfirmationDialog ->
				dialogState.value = SummaryDialog.RemoveProfilePictureConfirmation

			is Summary.Effect.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadSummaryAction()
	}

	when (val state = dialogState.value) {
		is SummaryDialog.OutdatedPassword ->
			onNavigateToUpdatePassword()

		is SummaryDialog.ProfilePictureSettings ->
			ProfilePictureSettingsDialog(
				sheetState = sheetState,
				showTake = state.showTake,
				showRemove = state.showRemove,
				onPickPictureClick = viewModel::pickProfilePictureAction,
				onTakePictureClick = viewModel::takeProfilePictureAction,
				onRemovePictureClick = viewModel::removeProfilePictureAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		is SummaryDialog.RemoveProfilePictureConfirmation ->
			RemoveProfilePictureConfirmationDialog(
				sheetState = sheetState,
				onConfirmClick = viewModel::confirmRemoveProfilePictureAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		null -> {}
	}

	SummaryScreen(
		state = viewState,
		onRetryClick = viewModel::loadSummaryAction,
		onEditProfilePictureClick = viewModel::openProfilePictureSettingsAction
	)
}