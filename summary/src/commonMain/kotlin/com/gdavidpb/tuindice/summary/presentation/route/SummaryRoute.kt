package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryItemsTextProvider
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureViewRenderer

@Composable
fun SummaryRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	profilePictureActions: ProfilePictureActions,
	profilePictureViewRenderer: ProfilePictureViewRenderer,
	summaryItemsTextProvider: SummaryItemsTextProvider,
	viewModel: SummaryViewModel
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

	SummaryScreen(
		state = viewState,
		onRetryClick = viewModel::loadSummaryAction,
		onEditProfilePictureClick = viewModel::openProfilePictureSettingsAction,
		profilePictureViewRenderer = profilePictureViewRenderer,
		summaryItemsTextProvider = summaryItemsTextProvider
	)
}
