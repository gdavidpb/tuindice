package com.gdavidpb.tuindice.utils.extension

import androidx.navigation.NavBackStackEntry
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

fun Flow<NavBackStackEntry>.mapBottomRoutes(destinations: List<Destination>) = mapNotNull { entry ->
	val targetRoute = entry
		.destination
		.route
		?.substringBefore("/")

	destinations.find { destination ->
		destination.route == targetRoute &&
				destination.isBottomDestination
	}?.route
}