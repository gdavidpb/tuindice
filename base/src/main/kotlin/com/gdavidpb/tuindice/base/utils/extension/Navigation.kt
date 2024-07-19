package com.gdavidpb.tuindice.base.utils.extension

import android.annotation.SuppressLint
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

private val destinationsMap = mapOf(
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

@SuppressLint("RestrictedApi")
inline fun <reified T : BaseViewModel<*, *, *>> NavController.viewModel(): T? {
	return visibleEntries.value.firstNotNullOfOrNull { backStackEntry ->
		backStackEntry.viewModelStore.let { viewModelStore ->
			viewModelStore.keys()
				.find { key -> key.endsWith("${T::class.qualifiedName}") }
				?.let(viewModelStore::get)
		}
	} as? T
}