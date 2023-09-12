package com.gdavidpb.tuindice.base.utils.extension

import android.net.Uri
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

fun NavController.navigatePopUpTo(destination: Destination) {
	while (popBackStack());

	navigate(route = destination.route) {
		launchSingleTop = true
	}
}

private fun buildTitleWithArgs(entry: NavBackStackEntry, destination: Destination) =
	destination.title.replace(titleArgsRegex) { match ->
		val key = match.value.trim('{', '}')

		entry
			.arguments
			?.getString(key, null)
			.let(Uri::decode)
			?: error("There is no value for '$key' arg.")
	}