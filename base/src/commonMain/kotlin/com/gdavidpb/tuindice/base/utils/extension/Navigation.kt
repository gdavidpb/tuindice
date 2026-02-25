package com.gdavidpb.tuindice.base.utils.extension

import androidx.navigation.NavController
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
