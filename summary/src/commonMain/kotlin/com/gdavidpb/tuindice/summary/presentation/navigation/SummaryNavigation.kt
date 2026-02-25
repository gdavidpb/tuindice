package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActions
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
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
	showSnackBar: (message: SnackBarMessage) -> Unit,
	profilePictureActionsFactory: @Composable (viewModel: SummaryViewModel) -> ProfilePictureActions,
	removeProfilePictureConfirmationDialogContent: @Composable (
		onConfirmClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	profilePictureSettingsDialogContent: @Composable (
		showRemove: Boolean,
		isCameraAvailable: Boolean,
		onPickPictureClick: () -> Unit,
		onTakePictureClick: () -> Unit,
		onRemovePictureClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	navigation<SummaryDestination.NavGraph>(startDestination = SummaryDestination.Summary) {
		composable<SummaryDestination.Summary> {
			val viewModel = koinInject<SummaryViewModel>()
			val profilePictureActions = profilePictureActionsFactory(viewModel)
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
			removeProfilePictureConfirmationDialogContent(onConfirmRemoveProfilePicture, onDismissRequest)
		}

		dialog<SummaryDestination.ProfilePictureSettingsDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<SummaryDestination.ProfilePictureSettingsDialog>()

			profilePictureSettingsDialogContent(
				args.showRemove,
				isCameraAvailable,
				onPickProfilePicture,
				onTakePicture,
				onRemoveProfilePicture,
				onDismissRequest
			)
		}
	}
}
