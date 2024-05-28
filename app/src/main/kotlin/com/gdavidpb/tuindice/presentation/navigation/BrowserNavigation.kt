package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.presentation.route.BrowserRoute

fun NavController.navigateToBrowser(title: String, url: String) {
	navigate(
		Destination.Browser(
			title = title,
			url = url
		)
	)
}

fun NavGraphBuilder.browserScreen() {
	composable<Destination.Browser> { backStackEntry ->
		val destination = backStackEntry.toRoute<Destination.Browser>()

		BrowserRoute(
			url = destination.url
		)
	}
}