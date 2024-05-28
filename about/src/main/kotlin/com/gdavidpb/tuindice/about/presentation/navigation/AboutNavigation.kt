package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

fun NavGraphBuilder.aboutScreen(
	navigateToBrowser: (title: String, url: String) -> Unit
) {
	composable<Destination.About> {
		AboutRoute(
			onNavigateToBrowser = navigateToBrowser
		)
	}
}