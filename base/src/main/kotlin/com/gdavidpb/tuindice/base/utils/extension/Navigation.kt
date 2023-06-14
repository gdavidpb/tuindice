package com.gdavidpb.tuindice.base.utils.extension

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

private val titleArgsRegex = "\\{\\w+\\}".toRegex()

fun Flow<NavBackStackEntry>.mapScreenDestination(destinations: Map<String, Destination>) =
	mapNotNull { entry ->
		val targetRoute = entry
			.destination
			.route
			?.substringBefore("/")

		val destination = destinations[targetRoute]

		if (destination != null && !destination.isDialogDestination) {
			val title = if (entry.arguments?.isEmpty == true)
				destination.title
			else
				buildTitleWithArgs(entry, destination)

			title to destination
		} else
			null
	}

fun NavController.navigateToSingleTop(route: String) {
	if (currentDestination?.route == route) return

	navigate(
		route = route
	) {
		launchSingleTop = true
		restoreState = true

		val currentRoute = currentDestination?.route

		if (currentRoute != null)
			popUpTo(currentRoute) {
				saveState = true
				inclusive = true
			}
	}
}

private fun buildTitleWithArgs(entry: NavBackStackEntry, destination: Destination) =
	destination.title.replace(titleArgsRegex) { match ->
		val key = match.value.trim('{', '}')

		entry.arguments?.getString(key, null) ?: error("There is no value for '$key' arg.")
	}