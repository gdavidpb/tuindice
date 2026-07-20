package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectNavResultWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationContentDialog
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.summaryEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	isCameraAvailable: Boolean,
	onNavigateToUpdatePassword: () -> Unit
) {
	summaryEntry(
		navActions = navActions,
		shellBindings = shellBindings,
		onNavigateToUpdatePassword = onNavigateToUpdatePassword
	)
	profilePictureSettingsDialogEntry(navActions = navActions, isCameraAvailable = isCameraAvailable)
	removeProfilePictureConfirmationDialogEntry(navActions = navActions)
}

private fun EntryProviderScope<NavKey>.summaryEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	onNavigateToUpdatePassword: () -> Unit
) {
	entry<SummaryDestination.Summary> {
		val viewModel = koinViewModel<SummaryViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CollectNavResultWithLifecycle<ProfilePictureSettingsResult>(awaitFrame = true) { result ->
			when (result) {
				ProfilePictureSettingsResult.Pick ->
					viewModel.pickProfilePictureAction()

				ProfilePictureSettingsResult.Take ->
					viewModel.takeProfilePictureAction()

				ProfilePictureSettingsResult.Remove ->
					viewModel.removeProfilePictureAction()
			}
		}

		CollectNavResultWithLifecycle<RemoveProfilePictureConfirmationResult> {
			viewModel.confirmRemoveProfilePictureAction()
		}

		SummaryRoute(
			onNavigateToUpdatePassword = onNavigateToUpdatePassword,
			onNavigateToProfilePictureSettingsDialog = { showRemove ->
				navActions.push(SummaryDestination.ProfilePictureSettingsDialog(showRemove = showRemove))
			},
			onNavigateToRemoveProfilePictureConfirmationDialog = {
				navActions.push(SummaryDestination.RemoveProfilePictureConfirmationDialog)
			},
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

private fun EntryProviderScope<NavKey>.profilePictureSettingsDialogEntry(
	navActions: TuIndiceNavActions,
	isCameraAvailable: Boolean
) {
	entry<SummaryDestination.ProfilePictureSettingsDialog>(metadata = dialogMetadata()) { key ->
		ProfilePictureSettingsContentDialog(
			showRemove = key.showRemove,
			isCameraAvailable = isCameraAvailable,
			onPickPictureClick = {
				navActions.popWithResult(ProfilePictureSettingsResult.Pick)
			},
			onTakePictureClick = {
				navActions.popWithResult(ProfilePictureSettingsResult.Take)
			},
			onRemovePictureClick = {
				navActions.popWithResult(ProfilePictureSettingsResult.Remove)
			},
			onDismissRequest = { navActions.pop() }
		)
	}
}

private fun EntryProviderScope<NavKey>.removeProfilePictureConfirmationDialogEntry(
	navActions: TuIndiceNavActions
) {
	entry<SummaryDestination.RemoveProfilePictureConfirmationDialog>(metadata = dialogMetadata()) {
		RemoveProfilePictureConfirmationContentDialog(
			onConfirmClick = {
				navActions.popWithResult(RemoveProfilePictureConfirmationResult.Confirmed)
			},
			onDismissRequest = { navActions.pop() }
		)
	}
}
