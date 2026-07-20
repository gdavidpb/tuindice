package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.gdavidpb.tuindice.base.presentation.navigation.LocalNavEntryScope
import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
inline fun <reified T : NavResult> CollectNavResultWithLifecycle(
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	crossinline onResult: (T) -> Unit
) {
	val scope = LocalNavEntryScope.current
	val lifecycle = LocalLifecycleOwner.current.lifecycle

	LaunchedEffect(scope, minActiveState, awaitFrame) {
		withContext(Dispatchers.Main.immediate) {
			snapshotFlow {
				if (scope.isCurrent.value) scope.resultStore.peek(scope.storeKey) else null
			}
				.flowWithLifecycle(lifecycle, minActiveState)
				.collect { result ->
					if (result !is T) return@collect

					scope.resultStore.consume(scope.storeKey)
					if (awaitFrame) withFrameNanos { }
					onResult(result)
				}
		}
	}
}
