package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel

@Composable
fun SummaryRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	profilePictureActions: ProfilePictureActions,
	viewModel: SummaryViewModel,
	content: @Composable (
		state: Summary.State,
		onRetryClick: () -> Unit,
		onEditProfilePictureClick: () -> Unit
	) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Summary.Effect.OpenCamera -> {
				profilePictureActions.openCamera(effect.output)
				viewModel.setCameraOutput(effect.output)
			}

			is Summary.Effect.OpenPicker ->
				profilePictureActions.openPicker()

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

	content(
		viewState,
		viewModel::loadSummaryAction,
		viewModel::openProfilePictureSettingsAction
	)
}
