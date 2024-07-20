package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute

fun NavGraphBuilder.aboutNavigation(
	navController: NavController
) {
	navigation<AboutDestination.NavGraph>(startDestination = AboutDestination.About) {
		composable<AboutDestination.About> {
			AboutRoute(
				onNavigateToBrowser = { title, url ->
					TODO()
				}
			)
		}
	}
}