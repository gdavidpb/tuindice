package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
inline fun <reified T> CollectEffectWithLifecycle(
	flow: Flow<T>,
	lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
	minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
	noinline action: suspend (T) -> Unit
) {
	LaunchedEffect(key1 = Unit) {
		lifecycleOwner.lifecycleScope.launch {
			flow.flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(action)
		}
	}
}