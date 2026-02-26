package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.presentation.route.BrowserRoute
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.browserNavigation(
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
		val viewModel = koinInject<BrowserViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		LaunchedEffect(viewState) {
			onViewStateChanged(viewState)
		}

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
		val hostUiTexts = koinInject<HostUiTextProvider>().getValues()

		ExternalResourceDialog(
			url = args.url,
			titleText = hostUiTexts.externalResourceTitle,
			messageText = hostUiTexts.externalResourceMessage,
			openText = hostUiTexts.externalResourceOpen,
			cancelText = hostUiTexts.externalResourceCancel,
			onConfirmClick = onNavigateToExternalResource,
			onDismissRequest = onDismissRequest
		)
	}
}
