package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
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
import com.gdavidpb.tuindice.base.utils.extension.navigateBackWithResult
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.screen.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.screen.RemoveProfilePictureConfirmationContentDialog
import org.koin.compose.viewmodel.koinViewModel

private const val PROFILE_PICTURE_SETTINGS_RESULT_KEY = "profile_picture_settings_result"
private const val REMOVE_PROFILE_PICTURE_CONFIRMATION_RESULT_KEY = "remove_profile_picture_confirmation_result"

private const val PROFILE_PICTURE_SETTINGS_RESULT_PICK = "pick"
private const val PROFILE_PICTURE_SETTINGS_RESULT_TAKE = "take"
private const val PROFILE_PICTURE_SETTINGS_RESULT_REMOVE = "remove"

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

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			navController.CollectBackResultWithLifecycle<String>(
				backStackEntry = backStackEntry,
				key = PROFILE_PICTURE_SETTINGS_RESULT_KEY,
				awaitFrame = true
			) { result ->
				when (result) {
					PROFILE_PICTURE_SETTINGS_RESULT_PICK ->
						viewModel.pickProfilePictureAction()

					PROFILE_PICTURE_SETTINGS_RESULT_TAKE ->
						viewModel.takeProfilePictureAction()

					PROFILE_PICTURE_SETTINGS_RESULT_REMOVE ->
						viewModel.removeProfilePictureAction()
				}
			}

			navController.CollectBackResultWithLifecycle<Boolean>(
				backStackEntry = backStackEntry,
				key = REMOVE_PROFILE_PICTURE_CONFIRMATION_RESULT_KEY
			) { confirmed ->
				if (confirmed)
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
					navController.navigateBackWithResult(
						key = PROFILE_PICTURE_SETTINGS_RESULT_KEY,
						result = PROFILE_PICTURE_SETTINGS_RESULT_PICK
					)
				},
				onTakePictureClick = {
					navController.navigateBackWithResult(
						key = PROFILE_PICTURE_SETTINGS_RESULT_KEY,
						result = PROFILE_PICTURE_SETTINGS_RESULT_TAKE
					)
				},
				onRemovePictureClick = {
					navController.navigateBackWithResult(
						key = PROFILE_PICTURE_SETTINGS_RESULT_KEY,
						result = PROFILE_PICTURE_SETTINGS_RESULT_REMOVE
					)
				},
				onDismissRequest = { navController.navigateUp() }
			)
		}

		dialog<SummaryDestination.RemoveProfilePictureConfirmationDialog> {
			RemoveProfilePictureConfirmationContentDialog(
				onConfirmClick = {
					navController.navigateBackWithResult(
						key = REMOVE_PROFILE_PICTURE_CONFIRMATION_RESULT_KEY,
						result = true
					)
				},
				onDismissRequest = { navController.navigateUp() }
			)
		}
	}
}
