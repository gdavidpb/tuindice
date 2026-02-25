package com.gdavidpb.tuindice.base.utils.extension

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.isActive

@Suppress("CAST_NEVER_SUCCEEDS")
inline fun <reified T : BaseViewModel<*, *, *>> NavController.viewModel(): T? {
	return visibleEntries.value.firstNotNullOfOrNull { backStackEntry ->
		backStackEntry.viewModelStore.let { viewModelStore ->
			viewModelStore.keys()
				.find { key -> key.endsWith("${T::class.qualifiedName}") }
				?.let(viewModelStore::get)
		}
	} as? T
}

@OptIn(ExperimentalCoroutinesApi::class)
fun NavController.viewModelFlow() =
	currentBackStackEntryFlow
		.filter { backStackEntry -> backStackEntry.destination.navigatorName != "dialog" }
		.mapLatest { backStackEntry ->
			val viewModelStore = backStackEntry.viewModelStore

			viewModelStore.keys()
				.mapNotNull { key -> viewModelStore[key] }
				.filterIsInstance<BaseViewModel<*, *, *>>()
				.find { viewModel -> viewModel.viewModelScope.isActive }!!
		}
		.retry { throwable ->
			delay(100)
			throwable is NullPointerException
		}

fun NavController.isCurrentDestination(destination: Destination): Boolean {
	return currentDestination?.parent?.route == destination::class.qualifiedName
}
