package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

@Composable
fun <T> CollectValueWithLifecycle(
	value: T,
	lifecycle: Lifecycle,
	isActive: Boolean = true,
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	onValue: (T) -> Unit
) {
	val currentValue = rememberUpdatedState(value)
	val currentOnValue = rememberUpdatedState(onValue)

	LaunchedEffect(lifecycle, isActive, minActiveState, awaitFrame) {
		if (!isActive) return@LaunchedEffect

		withContext(Dispatchers.Main.immediate) {
			snapshotFlow { currentValue.value }
				.flowWithLifecycle(lifecycle, minActiveState)
				.collect { collectedValue ->
					if (awaitFrame) withFrameNanos { }
					currentOnValue.value(collectedValue)
				}
		}
	}
}

@Composable
fun <T> NavController.CollectCurrentEntryValueWithLifecycle(
	backStackEntry: NavBackStackEntry,
	value: T,
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	onValue: (T) -> Unit
) {
	val currentBackStackEntry = currentBackStackEntryAsState().value

	CollectValueWithLifecycle(
		value = value,
		lifecycle = backStackEntry.lifecycle,
		isActive = currentBackStackEntry == backStackEntry,
		minActiveState = minActiveState,
		awaitFrame = awaitFrame,
		onValue = onValue
	)
}
