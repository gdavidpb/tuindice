package com.gdavidpb.tuindice.base.presentation.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gdavidpb.tuindice.base.presentation.route.BrowserRoute

data class BrowserArgs(
	val title: String,
	val url: String
)

fun NavController.navigateToBrowser(args: BrowserArgs) {
	val title = Uri.encode(args.title)
	val url = Uri.encode(args.url)

	navigate("${Destination.Browser.route}/$title/$url")
}

fun NavGraphBuilder.browserScreen() {
	composable(
		route = "${Destination.Browser.route}/{title}/{url}",
		arguments = listOf(
			navArgument("title") { type = NavType.StringType },
			navArgument("url") { type = NavType.StringType }
		)
	) { backStackEntry ->
		BrowserRoute(
			title = backStackEntry.arguments?.getString("title") ?: "",
			url = backStackEntry.arguments?.getString("url") ?: ""
		)
	}
}