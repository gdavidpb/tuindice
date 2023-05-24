package com.gdavidpb.tuindice.utils.extension

import androidx.navigation.NavBackStackEntry
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

private val titleArgsRegex = "\\{\\w+\\}".toRegex()

fun Flow<NavBackStackEntry>.mapDestination(destinations: Map<String, Destination>) =
	mapNotNull { entry ->
		val targetRoute = entry
			.destination
			.route
			?.substringBefore("/")

		val destination = destinations[targetRoute]

		if (destination != null) {
			val title = if (entry.arguments?.isEmpty == true)
				destination.title
			else
				buildTitleWithArgs(entry, destination)

			title to destination
		} else
			null
	}

private fun buildTitleWithArgs(entry: NavBackStackEntry, destination: Destination): String {
	return destination.title.replace(titleArgsRegex) { match ->
		val key = match.value.trim('{', '}')

		entry.arguments?.getString(key, null) ?: error("There is no value for '$key' arg.")
	}
}