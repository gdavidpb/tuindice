package com.gdavidpb.tuindice.auth.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.auth.presentation.route.SignInRoute
import com.gdavidpb.tuindice.auth.presentation.route.SignOutRoute
import com.gdavidpb.tuindice.auth.presentation.route.UpdatePasswordRoute
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectNavResultWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.authEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: AuthNavDependencies
) {
	signInEntry(shellBindings = shellBindings, dependencies = dependencies)
	signOutDialogEntry(navActions = navActions, shellBindings = shellBindings, dependencies = dependencies)
	updatePasswordDialogEntry(navActions = navActions, shellBindings = shellBindings, dependencies = dependencies)
}

private fun EntryProviderScope<NavKey>.signInEntry(
	shellBindings: NavShellBindings,
	dependencies: AuthNavDependencies
) {
	entry<AuthDestination.SignIn> {
		val viewModel = koinViewModel<SignInViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		SignInRoute(
			onNavigateToSummary = dependencies.onNavigateToSummary,
			onNavigateToBrowser = dependencies.onNavigateToBrowser,
			showSnackBar = shellBindings.showSnackBar,
			dismissSnackBar = shellBindings.dismissSnackBar,
			onOutdatedAppDetected = dependencies.onOutdatedAppDetected,
			viewModel = viewModel
		)
	}
}

private fun EntryProviderScope<NavKey>.signOutDialogEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: AuthNavDependencies
) {
	entry<AuthDestination.SignOutDialog>(metadata = dialogMetadata()) { key ->
		val viewModel = koinViewModel<SignOutViewModel>()
		val pendingChanges = PendingChanges(
			totalCount = key.totalCount,
			recordCount = key.recordCount,
			evaluationsCount = key.evaluationsCount,
			hasFailedMutations = key.hasFailedMutations
		)

		CollectNavResultWithLifecycle<UpdatePasswordBackResult>(awaitFrame = true) { result ->
			when (result) {
				UpdatePasswordBackResult.PasswordUpdated ->
					viewModel.retryFlushAndSignOutAction(pendingChanges)
			}
		}

		SignOutRoute(
			initialPendingChanges = pendingChanges,
			onNavigateToSignIn = dependencies.onNavigateToSignIn,
			onNavigateToUpdatePassword = {
				navActions.push(AuthDestination.UpdatePasswordDialog)
			},
			onDismissRequest = { navActions.pop() },
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

private fun EntryProviderScope<NavKey>.updatePasswordDialogEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: AuthNavDependencies
) {
	entry<AuthDestination.UpdatePasswordDialog>(metadata = dialogMetadata()) {
		val viewModel = koinViewModel<UpdatePasswordViewModel>()

		UpdatePasswordRoute(
			onDismissRequest = dependencies.onUpdatePasswordDismissRequest,
			onPasswordUpdated = {
				navActions.popWithResult(UpdatePasswordBackResult.PasswordUpdated)
			},
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}
