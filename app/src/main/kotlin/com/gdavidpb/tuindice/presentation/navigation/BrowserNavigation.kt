package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.utils.extension.browse
import com.gdavidpb.tuindice.presentation.route.BrowserRoute

fun NavGraphBuilder.browserNavigation(
	navController: NavController
) {
	composable<BrowserDestination.Browser> { backStackEntry ->
		val destination = backStackEntry.toRoute<BrowserDestination.Browser>()

		BrowserRoute(
			url = destination.url,
			onNavigateToExternalResourceDialog = { url ->
				navController.navigate(
					BrowserDestination.ExternalResourceDialog(
						url = url
					)
				)
			}
		)
	}

	dialog<BrowserDestination.ExternalResourceDialog> { backStackEntry ->
		val args = backStackEntry.toRoute<BrowserDestination.ExternalResourceDialog>()

		val context = LocalContext.current

		ExternalResourceDialog(
			url = args.url,
			onConfirmClick = { url ->
				context.browse(url)
			},
			onDismissRequest = {
				navController.navigateUp()
			}
		)
	}
}