package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.presentation.route.BrowserRoute
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.cancel
import tuindice.maincore.generated.resources.dialog_message_warning_external
import tuindice.maincore.generated.resources.dialog_title_warning_external
import tuindice.maincore.generated.resources.open

fun NavGraphBuilder.browserNavigation(
	navController: NavHostController,
	onNavigateToExternalResourceDialog: (url: String) -> Unit,
	onNavigateToExternalResource: (url: String) -> Unit,
	onDismissRequest: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit
) {
	composable<BrowserDestination.Browser>(
		typeMap = emptyMap(),
		deepLinks = emptyList(),
		enterTransition = null,
		exitTransition = null,
		popEnterTransition = null,
		popExitTransition = null,
		sizeTransform = null
	) { backStackEntry ->
		val args = backStackEntry.toRoute<BrowserDestination.Browser>()
		val viewModel = koinViewModel<BrowserViewModel>(viewModelStoreOwner = backStackEntry)
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		navController.CollectCurrentEntryValueWithLifecycle(
			backStackEntry = backStackEntry,
			value = viewState,
			onValue = onViewStateChanged
		)

		BrowserRoute(
			title = args.title,
			url = args.url,
			onNavigateToExternalResourceDialog = onNavigateToExternalResourceDialog,
			viewModel = viewModel
		)
	}

	dialog<BrowserDestination.ExternalResourceDialog>(
		typeMap = emptyMap(),
		deepLinks = emptyList(),
		dialogProperties = DialogProperties()
	) { backStackEntry ->
		val args = backStackEntry.toRoute<BrowserDestination.ExternalResourceDialog>()

		ExternalResourceDialog(
			url = args.url,
			titleText = stringResource(Res.string.dialog_title_warning_external),
			messageText = stringResource(Res.string.dialog_message_warning_external),
			openText = stringResource(Res.string.open),
			cancelText = stringResource(Res.string.cancel),
			onConfirmClick = onNavigateToExternalResource,
			onDismissRequest = onDismissRequest
		)
	}
}
