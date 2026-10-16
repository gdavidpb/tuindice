package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * Runs [action] every time the screen comes back in front (RESUMED), the first time included: what
 * a screen that follows the clock does to read it again after the process was suspended. The same
 * mechanism the app host uses to ask for a sync; [action] is only called, never launched with
 * work of its own, so it should hand the work to the screen's view model.
 */
@Composable
fun ResumedEffect(
	lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
	action: () -> Unit
) {
	val currentAction = rememberUpdatedState(action)

	LaunchedEffect(lifecycleOwner) {
		lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			currentAction.value()
		}
	}
}
