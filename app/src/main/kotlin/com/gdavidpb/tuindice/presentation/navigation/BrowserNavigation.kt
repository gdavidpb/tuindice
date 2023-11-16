package com.gdavidpb.tuindice.presentation.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

fun NavController.navigateToBrowser(title: String, url: String) {
	navigate("${Destination.Browser.route}/${Uri.encode(title)}/${Uri.encode(url)}")
}

fun NavGraphBuilder.browserScreen() {
	composable(
		route = "${Destination.Browser.route}/{title}/{url}",
		arguments = listOf(
			navArgument("title") { type = NavType.StringType },
			navArgument("url") { type = NavType.StringType }
		)
	) { backStackEntry ->
		com.gdavidpb.tuindice.presentation.route.BrowserRoute(
			title = backStackEntry.arguments?.getString("title") ?: "",
			url = backStackEntry.arguments?.getString("url") ?: ""
		)
	}
}