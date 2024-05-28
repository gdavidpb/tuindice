package com.gdavidpb.tuindice.base.utils.extension

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

private val destinationsMap = mapOf(
	Destination.Summary.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.Summary>() },
	Destination.Record.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.Record>() },
	Destination.Evaluations.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.Evaluations>() },
	Destination.About.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.About>() },
	Destination.EnrollmentProofFetch.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.EnrollmentProofFetch>() },
	Destination.UpdatePassword.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.UpdatePassword>() },
	Destination.SignIn.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.SignIn>() },
	Destination.SignOut.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.SignOut>() },
	Destination.Browser.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.Browser>() },
	Destination.Evaluation.serializer()
		.hashCode() to { entry: NavBackStackEntry -> entry.toRoute<Destination.Evaluation>() },
)

fun Flow<NavBackStackEntry>.mapDestination() =
	mapNotNull { backStackEntry ->
		destinationsMap[backStackEntry.destination.id]
			?.let { toRoute -> toRoute(backStackEntry) }
			?.takeIf { destination -> !destination.isDialogDestination }
	}

fun NavController.navigatePopUpTo(destination: Destination) {
	while (popBackStack()) Unit

	navigate(route = destination) {
		launchSingleTop = true
	}
}