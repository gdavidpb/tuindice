package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.screen.RemoveProfilePictureConfirmationContentDialog
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureViewRenderer
import org.koin.compose.koinInject

private const val PROFILE_PICTURE_DIALOG_ACTION_KEY = "profile_picture_dialog_action"
private const val CONFIRM_REMOVE_PROFILE_PICTURE_KEY = "confirm_remove_profile_picture"
private const val PROFILE_PICTURE_ACTION_PICK = "pick"
private const val PROFILE_PICTURE_ACTION_TAKE = "take"
private const val PROFILE_PICTURE_ACTION_REMOVE = "remove"

fun NavGraphBuilder.summaryNavigation(
	navController: NavHostController,
	isCameraAvailable: Boolean,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	onDismissRequest: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<SummaryDestination.NavGraph>(startDestination = SummaryDestination.Summary) {
		composable<SummaryDestination.Summary> { backStackEntry ->
			val viewModel = koinInject<SummaryViewModel>()
			val profilePictureActionsFactory = koinInject<ProfilePictureActionsFactory>()
			val profilePictureViewRenderer = koinInject<ProfilePictureViewRenderer>()
			val profilePictureActions = profilePictureActionsFactory.remember(
				onPicturePicked = viewModel::uploadProfilePictureAction,
				onPictureTaken = viewModel::uploadTakenProfilePictureAction
			)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			LaunchedEffect(backStackEntry, viewModel) {
				backStackEntry.savedStateHandle
					.getStateFlow<String?>(PROFILE_PICTURE_DIALOG_ACTION_KEY, null)
					.collect { action ->
						when (action) {
							PROFILE_PICTURE_ACTION_PICK ->
								viewModel.pickProfilePictureAction()

							PROFILE_PICTURE_ACTION_TAKE ->
								viewModel.takeProfilePictureAction()

							PROFILE_PICTURE_ACTION_REMOVE ->
								viewModel.removeProfilePictureAction()

							null -> Unit
							else -> Unit
						}

						if (action != null)
							backStackEntry.savedStateHandle[PROFILE_PICTURE_DIALOG_ACTION_KEY] = null
					}
			}

			LaunchedEffect(backStackEntry, viewModel) {
				backStackEntry.savedStateHandle
					.getStateFlow(CONFIRM_REMOVE_PROFILE_PICTURE_KEY, false)
					.collect { confirmed ->
						if (!confirmed)
							return@collect

						viewModel.confirmRemoveProfilePictureAction()
						backStackEntry.savedStateHandle[CONFIRM_REMOVE_PROFILE_PICTURE_KEY] = false
					}
			}

			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = onNavigateToProfilePictureSettingsDialog,
				onNavigateToRemoveProfilePictureConfirmationDialog = onNavigateToRemoveProfilePictureConfirmationDialog,
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				showSnackBar = showSnackBar,
				profilePictureActions = profilePictureActions,
				profilePictureViewRenderer = profilePictureViewRenderer,
				viewModel = viewModel
			)
		}

		dialog<SummaryDestination.RemoveProfilePictureConfirmationDialog> {
			val resultHandle = navController.previousBackStackEntry?.savedStateHandle

			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = {
					resultHandle?.set(CONFIRM_REMOVE_PROFILE_PICTURE_KEY, true)
				},
				onDismissRequest = onDismissRequest
			)
		}

		dialog<SummaryDestination.ProfilePictureSettingsDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<SummaryDestination.ProfilePictureSettingsDialog>()
			val resultHandle = navController.previousBackStackEntry?.savedStateHandle

			ProfilePictureSettingsContentDialog(
				showRemove = args.showRemove,
				isCameraAvailable = isCameraAvailable,
				onPickPictureClick = {
					resultHandle?.set(PROFILE_PICTURE_DIALOG_ACTION_KEY, PROFILE_PICTURE_ACTION_PICK)
				},
				onTakePictureClick = {
					resultHandle?.set(PROFILE_PICTURE_DIALOG_ACTION_KEY, PROFILE_PICTURE_ACTION_TAKE)
				},
				onRemovePictureClick = {
					resultHandle?.set(PROFILE_PICTURE_DIALOG_ACTION_KEY, PROFILE_PICTURE_ACTION_REMOVE)
				},
				onDismissRequest = onDismissRequest
			)
		}
	}
}
