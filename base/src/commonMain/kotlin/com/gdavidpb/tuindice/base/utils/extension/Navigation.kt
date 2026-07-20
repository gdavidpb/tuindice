package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.gdavidpb.tuindice.base.presentation.navigation.LocalNavEntryScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
fun <T> CollectCurrentEntryValueWithLifecycle(
	value: T,
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	onValue: (T) -> Unit
) {
	val entryScope = LocalNavEntryScope.current

	CollectValueWithLifecycle(
		value = value,
		lifecycle = LocalLifecycleOwner.current.lifecycle,
		isActive = entryScope.isCurrent.value,
		minActiveState = minActiveState,
		awaitFrame = awaitFrame,
		onValue = onValue
	)
}
