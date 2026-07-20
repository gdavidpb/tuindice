package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectNavResultWithLifecycle
import com.gdavidpb.tuindice.record.presentation.route.CreateSyntheticTermRoute
import com.gdavidpb.tuindice.record.presentation.route.RecordRoute
import com.gdavidpb.tuindice.record.presentation.route.toRouteViewState
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.ui.dialog.DeleteSyntheticTermConfirmationContentDialog
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.recordEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: RecordNavDependencies
) {
	recordEntry(navActions = navActions, shellBindings = shellBindings, dependencies = dependencies)
	createSyntheticTermEntry(navActions = navActions, shellBindings = shellBindings, dependencies = dependencies)
	deleteSyntheticTermConfirmationDialogEntry(navActions = navActions)
}

private fun EntryProviderScope<NavKey>.recordEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: RecordNavDependencies
) {
	entry<RecordDestination.Record> {
		val viewModel = koinViewModel<RecordViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState.toRouteViewState(),
			onValue = shellBindings.onViewStateChanged
		)

		CollectNavResultWithLifecycle<DeleteSyntheticTermConfirmationResult> { result ->
			when (result) {
				is DeleteSyntheticTermConfirmationResult.Confirmed ->
					viewModel.deleteSyntheticTermAction(result.termId)
			}
		}

		RecordRoute(
			onNavigateToUpdatePassword = dependencies.onNavigateToUpdatePassword,
			onNavigateToCreateSyntheticTerm = {
				navActions.push(RecordDestination.CreateSyntheticTerm())
			},
			onNavigateToUpdateSyntheticTerm = { termId ->
				navActions.push(RecordDestination.CreateSyntheticTerm(termId = termId))
			},
			onNavigateToDeleteSyntheticTermConfirmation = { termId ->
				navActions.push(RecordDestination.DeleteSyntheticTermConfirmationDialog(termId = termId))
			},
			onTopBarViewModeChangeAvailable = dependencies.onTopBarViewModeChangeAvailable,
			onTopBarTermSelectionAvailable = dependencies.onTopBarTermSelectionAvailable,
			onNavigateToEnrollmentProof = dependencies.onNavigateToEnrollmentProof,
			showTopBarBanner = shellBindings.showTopBarBanner,
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

private fun EntryProviderScope<NavKey>.createSyntheticTermEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: RecordNavDependencies
) {
	entry<RecordDestination.CreateSyntheticTerm> { key ->
		val viewModel = koinViewModel<CreateSyntheticTermViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CreateSyntheticTermRoute(
			termId = key.termId,
			viewModel = viewModel,
			onBack = { navActions.pop() },
			onBackInterceptorAvailable = shellBindings.onBackInterceptorAvailable,
			onSubjectStatsClick = dependencies.onNavigateToSubjectDetail
		)
	}
}

private fun EntryProviderScope<NavKey>.deleteSyntheticTermConfirmationDialogEntry(
	navActions: TuIndiceNavActions
) {
	entry<RecordDestination.DeleteSyntheticTermConfirmationDialog>(metadata = dialogMetadata()) { key ->
		DeleteSyntheticTermConfirmationContentDialog(
			onConfirmClick = {
				navActions.popWithResult(DeleteSyntheticTermConfirmationResult.Confirmed(key.termId))
			},
			onDismissRequest = { navActions.pop() }
		)
	}
}
