package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
inline fun <reified T> CollectEffectWithLifecycle(
	flow: Flow<T>,
	lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
	minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
	noinline action: suspend (T) -> Unit
) {
	LaunchedEffect(flow, lifecycleOwner.lifecycle) {
		withContext(Dispatchers.Main.immediate) {
			flow
				.flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
				.collect(action)
		}
	}
}