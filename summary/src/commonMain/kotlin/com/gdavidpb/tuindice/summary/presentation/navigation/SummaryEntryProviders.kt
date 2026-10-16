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
import com.gdavidpb.tuindice.summary.presentation.model.StatisticsTopBarActionBus
import com.gdavidpb.tuindice.summary.presentation.route.ShareTermRoute
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute
import com.gdavidpb.tuindice.summary.presentation.route.SummaryStatisticsRoute
import com.gdavidpb.tuindice.summary.presentation.viewmodel.ShareTermViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryStatisticsViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationContentDialog
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.summaryEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	navigation: SummaryNavDependencies
) {
	summaryEntry(
		navActions = navActions,
		shellBindings = shellBindings,
		navigation = navigation
	)
	statisticsEntry(
		navActions = navActions,
		shellBindings = shellBindings,
		navigation = navigation
	)
	shareTermDialogEntry(navActions = navActions)
	profilePictureSettingsDialogEntry(navActions = navActions)
	removeProfilePictureConfirmationDialogEntry(navActions = navActions)
}

private fun EntryProviderScope<NavKey>.summaryEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	navigation: SummaryNavDependencies
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
			onNavigateToProfilePictureSettingsDialog = { showRemove, isCameraAvailable ->
				navActions.push(
					SummaryDestination.ProfilePictureSettingsDialog(
						showRemove = showRemove,
						isCameraAvailable = isCameraAvailable
					)
				)
			},
			onNavigateToRemoveProfilePictureConfirmationDialog = {
				navActions.push(SummaryDestination.RemoveProfilePictureConfirmationDialog)
			},
			showSnackBar = shellBindings.showSnackBar,
			navigation = navigation,
			viewModel = viewModel
		)
	}
}

// The Statistics screen, pushed over the hub: it is read-only, so it has no result to send back.
private fun EntryProviderScope<NavKey>.statisticsEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	navigation: SummaryNavDependencies
) {
	entry<SummaryDestination.Statistics> {
		val viewModel = koinViewModel<SummaryStatisticsViewModel>()
		val topBarActionBus = koinInject<StatisticsTopBarActionBus>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		SummaryStatisticsRoute(
			navigation = navigation,
			viewModel = viewModel,
			topBarActionBus = topBarActionBus,
			onNavigateToShareTerm = {
				navActions.push(SummaryDestination.ShareTermDialog(termKey = null))
			}
		)
	}
}

// The sheet that shares a closed term, over the screen it was opened from. It has no result to send back.
private fun EntryProviderScope<NavKey>.shareTermDialogEntry(
	navActions: TuIndiceNavActions
) {
	entry<SummaryDestination.ShareTermDialog>(metadata = dialogMetadata()) { key ->
		val viewModel = koinViewModel<ShareTermViewModel>()

		ShareTermRoute(
			termKey = key.termKey,
			viewModel = viewModel,
			onDismissRequest = { navActions.pop() }
		)
	}
}

private fun EntryProviderScope<NavKey>.profilePictureSettingsDialogEntry(
	navActions: TuIndiceNavActions
) {
	entry<SummaryDestination.ProfilePictureSettingsDialog>(metadata = dialogMetadata()) { key ->
		ProfilePictureSettingsContentDialog(
			showRemove = key.showRemove,
			isCameraAvailable = key.isCameraAvailable,
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
