package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectBackResultWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.navigateBackWithResult
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationContentDialog
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.summaryNavigation(
	navController: NavHostController,
	isCameraAvailable: Boolean,
	onNavigateToUpdatePassword: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<SummaryDestination.NavGraph>(startDestination = SummaryDestination.Summary) {
		composable<SummaryDestination.Summary> { backStackEntry ->
			val viewModel = koinViewModel<SummaryViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			navController.CollectBackResultWithLifecycle<ProfilePictureSettingsResult>(
				backStackEntry = backStackEntry,
				awaitFrame = true
			) { result ->
				when (result) {
					ProfilePictureSettingsResult.Pick ->
						viewModel.pickProfilePictureAction()

					ProfilePictureSettingsResult.Take ->
						viewModel.takeProfilePictureAction()

					ProfilePictureSettingsResult.Remove ->
						viewModel.removeProfilePictureAction()
				}
			}

			navController.CollectBackResultWithLifecycle<RemoveProfilePictureConfirmationResult>(
				backStackEntry = backStackEntry
			) {
				viewModel.confirmRemoveProfilePictureAction()
			}

			SummaryRoute(
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					navController.navigate(
						SummaryDestination.ProfilePictureSettingsDialog(showRemove = showRemove)
					)
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {
					navController.navigate(SummaryDestination.RemoveProfilePictureConfirmationDialog)
				},
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		dialog<SummaryDestination.ProfilePictureSettingsDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<SummaryDestination.ProfilePictureSettingsDialog>()

			ProfilePictureSettingsContentDialog(
				showRemove = args.showRemove,
				isCameraAvailable = isCameraAvailable,
				onPickPictureClick = {
					navController.navigateBackWithResult<ProfilePictureSettingsResult>(
						ProfilePictureSettingsResult.Pick
					)
				},
				onTakePictureClick = {
					navController.navigateBackWithResult<ProfilePictureSettingsResult>(
						ProfilePictureSettingsResult.Take
					)
				},
				onRemovePictureClick = {
					navController.navigateBackWithResult<ProfilePictureSettingsResult>(
						ProfilePictureSettingsResult.Remove
					)
				},
				onDismissRequest = { navController.navigateUp() }
			)
		}

		dialog<SummaryDestination.RemoveProfilePictureConfirmationDialog> {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = {
					navController.navigateBackWithResult<RemoveProfilePictureConfirmationResult>(
						RemoveProfilePictureConfirmationResult.Confirmed
					)
				},
				onDismissRequest = { navController.navigateUp() }
			)
		}
	}
}
