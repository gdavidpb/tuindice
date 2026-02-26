package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.koin.compose.koinInject

fun NavGraphBuilder.summaryNavigation(
	isCameraAvailable: Boolean,
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	onConfirmRemoveProfilePicture: () -> Unit,
	onPickProfilePicture: () -> Unit,
	onTakePicture: () -> Unit,
	onRemoveProfilePicture: () -> Unit,
	onDismissRequest: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<SummaryDestination.NavGraph>(startDestination = SummaryDestination.Summary) {
		composable<SummaryDestination.Summary> {
			val viewModel = koinInject<SummaryViewModel>()
			val profilePictureActionsFactory = koinInject<ProfilePictureActionsFactory>()
			val profilePictureActions = profilePictureActionsFactory.remember(
				onPicturePicked = viewModel::uploadProfilePictureAction,
				onPictureTaken = viewModel::uploadTakenProfilePictureAction
			)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = onNavigateToProfilePictureSettingsDialog,
				onNavigateToRemoveProfilePictureConfirmationDialog = onNavigateToRemoveProfilePictureConfirmationDialog,
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				showSnackBar = showSnackBar,
				profilePictureActions = profilePictureActions,
				viewModel = viewModel
			)
		}

		dialog<SummaryDestination.RemoveProfilePictureConfirmationDialog> {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = onConfirmRemoveProfilePicture,
				onDismissRequest = onDismissRequest
			)
		}

		dialog<SummaryDestination.ProfilePictureSettingsDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<SummaryDestination.ProfilePictureSettingsDialog>()

			ProfilePictureSettingsContentDialog(
				showRemove = args.showRemove,
				isCameraAvailable = isCameraAvailable,
				onPickPictureClick = onPickProfilePicture,
				onTakePictureClick = onTakePicture,
				onRemovePictureClick = onRemoveProfilePicture,
				onDismissRequest = onDismissRequest
			)
		}
	}
}
