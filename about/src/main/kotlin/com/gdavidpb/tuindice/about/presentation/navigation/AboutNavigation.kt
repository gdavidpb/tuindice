package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute

fun NavGraphBuilder.aboutNavigation(
	onNavigateToBrowser: (title: String, url: String) -> Unit
) {
	navigation<AboutDestination.NavGraph>(startDestination = AboutDestination.About) {
		composable<AboutDestination.About> {
			AboutRoute(
				onNavigateToBrowser = onNavigateToBrowser
			)
		}
	}
}