package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.presentation.route.BrowserRoute

fun NavGraphBuilder.browserNavigation(
	onNavigateToExternalResourceDialog: (url: String) -> Unit,
	onNavigateToExternalResource: (url: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	composable<BrowserDestination.Browser> { backStackEntry ->
		val destination = backStackEntry.toRoute<BrowserDestination.Browser>()

		BrowserRoute(
			url = destination.url,
			onNavigateToExternalResourceDialog = onNavigateToExternalResourceDialog
		)
	}

	dialog<BrowserDestination.ExternalResourceDialog> { backStackEntry ->
		val args = backStackEntry.toRoute<BrowserDestination.ExternalResourceDialog>()

		ExternalResourceDialog(
			url = args.url,
			onConfirmClick = onNavigateToExternalResource,
			onDismissRequest = onDismissRequest
		)
	}
}