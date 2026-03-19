package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T> NavController.setBackResult(key: String, result: T): Boolean {
	val previousBackStackEntry = previousBackStackEntry ?: return false

	previousBackStackEntry.savedStateHandle[key] = result

	return true
}

fun <T> NavController.navigateBackWithResult(key: String, result: T): Boolean {
	if (!setBackResult(key = key, result = result)) return false

	return navigateUp()
}

@Composable
inline fun <reified T> NavController.CollectBackResultWithLifecycle(
	backStackEntry: NavBackStackEntry,
	key: String,
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	crossinline onResult: (T) -> Unit
) {
	val currentBackStackEntry = currentBackStackEntryAsState().value

	LaunchedEffect(backStackEntry, key, currentBackStackEntry, minActiveState, awaitFrame) {
		if (currentBackStackEntry != backStackEntry) return@LaunchedEffect

		withContext(Dispatchers.Main.immediate) {
			backStackEntry.savedStateHandle
				.getStateFlow<T?>(key, null)
				.flowWithLifecycle(backStackEntry.lifecycle, minActiveState)
				.collect { result ->
					if (result == null) return@collect

					if (awaitFrame) withFrameNanos { }

					onResult(result)
					backStackEntry.savedStateHandle[key] = null
				}
		}
	}
}
