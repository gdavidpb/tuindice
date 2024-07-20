package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationDialog

fun NavGraphBuilder.summaryNavigation(
	onNavigateToProfilePictureSettingsDialog: (showRemove: Boolean) -> Unit,
	onNavigateToUpdatePassword: () -> Unit,
	onNavigateToRemoveProfilePictureConfirmationDialog: () -> Unit,
	onConfirmRemoveProfilePicture: () -> Unit,
	onPickProfilePicture: () -> Unit,
	onTakePicture: () -> Unit,
	onRemoveProfilePicture: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<SummaryDestination.NavGraph>(startDestination = SummaryDestination.Summary) {
		composable<SummaryDestination.Summary> {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = onNavigateToProfilePictureSettingsDialog,
				onNavigateToRemoveProfilePictureConfirmationDialog = onNavigateToRemoveProfilePictureConfirmationDialog,
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				showSnackBar = showSnackBar
			)
		}

		dialog<SummaryDestination.RemoveProfilePictureConfirmationDialog> {
			RemoveProfilePictureConfirmationDialog(
				onConfirmClick = onConfirmRemoveProfilePicture,
				onDismissRequest = onDismissRequest
			)
		}

		dialog<SummaryDestination.ProfilePictureSettingsDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<SummaryDestination.ProfilePictureSettingsDialog>()

			ProfilePictureSettingsDialog(
				showRemove = args.showRemove,
				onPickPictureClick = onPickProfilePicture,
				onTakePictureClick = onTakePicture,
				onRemovePictureClick = onRemoveProfilePicture,
				onDismissRequest = onDismissRequest
			)
		}
	}
}