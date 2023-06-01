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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: SummaryViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val sheetState = rememberModalBottomSheetState()
	val dialogState = remember { mutableStateOf<SummaryDialog?>(null) }

	val pickVisualMediaRequest = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia(),
		onResult = { uri -> if (uri != null) viewModel.uploadProfilePictureAction(path = "$uri") }
	)

	val registerTakePicture = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.TakePicture(),
		onResult = { result -> if (result) viewModel.uploadTakenProfilePictureAction() }
	)

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Summary.Event.OpenCamera -> {
				registerTakePicture.launch(Uri.parse(event.output))

				viewModel.setCameraOutput(event.output)
			}

			is Summary.Event.OpenPicker ->
				pickVisualMediaRequest.launch(
					PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
				)

			is Summary.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)

			is Summary.Event.ShowProfilePictureSettingsDialog ->
				dialogState.value = SummaryDialog.ProfilePictureSettings(
					showTake = context.hasCamera(),
					showRemove = event.showRemove
				)

			Summary.Event.ShowRemoveProfilePictureConfirmationDialog ->
				dialogState.value = SummaryDialog.RemoveProfilePictureConfirmation

			is Summary.Event.CloseDialog ->
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