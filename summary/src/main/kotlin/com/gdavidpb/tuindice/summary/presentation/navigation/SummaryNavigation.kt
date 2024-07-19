package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.viewModel
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationDialog

fun NavGraphBuilder.summaryNavigation(
	navController: NavController,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<SummaryDestination.NavGraph>(startDestination = SummaryDestination.SummaryScreen) {
		composable<SummaryDestination.SummaryScreen> {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					navController.navigate(
						SummaryDestination.ProfilePictureSettingsDialog(
							showRemove = showRemove
						)
					)
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {
					SummaryDestination.RemoveProfilePictureConfirmationDialog
				},
				onNavigateToUpdatePassword = {
					navController.navigate(Destination.UpdatePassword) // TODO
				},
				showSnackBar = showSnackBar
			)
		}

		dialog<SummaryDestination.RemoveProfilePictureConfirmationDialog> {
			RemoveProfilePictureConfirmationDialog(
				onConfirmClick = {
					navController
						.viewModel<SummaryViewModel>()
						?.confirmRemoveProfilePictureAction()
				},
				onDismissRequest = {
					navController.navigateUp()
				}
			)
		}

		dialog<SummaryDestination.ProfilePictureSettingsDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<SummaryDestination.ProfilePictureSettingsDialog>()

			ProfilePictureSettingsDialog(
				showRemove = args.showRemove,
				onPickPictureClick = {
					navController
						.viewModel<SummaryViewModel>()
						?.pickProfilePictureAction()
				},
				onTakePictureClick = {
					navController
						.viewModel<SummaryViewModel>()
						?.takeProfilePictureAction()
				},
				onRemovePictureClick = {
					navController
						.viewModel<SummaryViewModel>()
						?.removeProfilePictureAction()
				},
				onDismissRequest = {
					navController.navigateUp()
				}
			)
		}
	}
}