package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.base.presentation.navigation.BrowserArgs
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

fun NavController.navigateToAbout() {
	navigate(Destination.About.route)
}

fun NavGraphBuilder.aboutScreen(
	navigateToBrowser: (args: BrowserArgs) -> Unit
) {
	composable(Destination.About.route) {
		AboutRoute(
			onNavigateToBrowser = { title, url ->
				navigateToBrowser(
					BrowserArgs(
						title = title,
						url = url
					)
				)
			}
		)
	}
}