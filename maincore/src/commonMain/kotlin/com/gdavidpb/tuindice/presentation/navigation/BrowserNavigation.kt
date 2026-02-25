package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.route.BrowserRoute
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.browserNavigation(
	onNavigateToExternalResourceDialog: (url: String) -> Unit,
	onNavigateToExternalResource: (url: String) -> Unit,
	onDismissRequest: () -> Unit,
	browserContent: @Composable (
		state: Browser.State,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
	) -> Unit,
	externalResourceDialogContent: @Composable (
		url: String,
		onConfirmClick: (url: String) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
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

		BrowserRoute(
			title = args.title,
			url = args.url,
			onNavigateToExternalResourceDialog = onNavigateToExternalResourceDialog,
			viewModel = viewModel,
			content = browserContent
		)
	}

	dialog<BrowserDestination.ExternalResourceDialog>(
		typeMap = emptyMap(),
		deepLinks = emptyList(),
		dialogProperties = DialogProperties()
	) { backStackEntry ->
		val args = backStackEntry.toRoute<BrowserDestination.ExternalResourceDialog>()

		externalResourceDialogContent(args.url, onNavigateToExternalResource, onDismissRequest)
	}
}
