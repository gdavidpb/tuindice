package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.navigation.FloatingWindow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel

inline fun <reified T : BaseViewModel<*, *, *>> NavController.viewModel(): T? {
	return visibleEntries.value.firstNotNullOfOrNull { backStackEntry ->
		backStackEntry.viewModelStore.let { viewModelStore ->
			viewModelStore.keys()
				.find { key -> key.endsWith("${T::class.qualifiedName}") }
				?.let(viewModelStore::get)
		}
	} as? T
}

fun NavController.isCurrentDestination(destination: Destination): Boolean {
	return currentDestination?.parent?.route == destination::class.qualifiedName
}

@Composable
fun NavController.canNavigateBackFromCurrentDestination(): Boolean {
	val currentBackStackEntry = currentBackStackEntryAsState().value
	val destination = currentDestination ?: return false
	return currentBackStackEntry != null &&
		previousBackStackEntry != null &&
		destination !is FloatingWindow
}
