package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gdavidpb.tuindice.base.logging.appLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@PublishedApi
internal val navigationResultJson = Json {
	ignoreUnknownKeys = true
}

@PublishedApi
internal val navigationResultLogger = appLogger(tag = "NavigationResult")

@PublishedApi
internal inline fun <reified T : Any> backResultKey(): String =
	requireNotNull(T::class.qualifiedName) {
		"Back result type ${T::class.simpleName} must have a qualified name"
	}

inline fun <reified T : Any> NavController.setBackResult(result: T): Boolean {
	val previousBackStackEntry = previousBackStackEntry ?: return false
	val key = backResultKey<T>()
	val serializedResult = runCatching {
		navigationResultJson.encodeToString<T>(result)
	}.getOrElse { throwable ->
		navigationResultLogger.w(throwable) { "Failed to encode back result '$key'." }
		return false
	}

	previousBackStackEntry.savedStateHandle[key] = serializedResult

	return true
}

inline fun <reified T : Any> NavController.navigateBackWithResult(result: T): Boolean {
	if (!setBackResult(result = result)) return false

	return navigateUp()
}

@Composable
inline fun <reified T : Any> NavController.CollectBackResultWithLifecycle(
	backStackEntry: NavBackStackEntry,
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	crossinline onResult: (T) -> Unit
) {
	val key = backResultKey<T>()
	val currentBackStackEntry = currentBackStackEntryAsState().value

	LaunchedEffect(backStackEntry, key, currentBackStackEntry, minActiveState, awaitFrame) {
		if (currentBackStackEntry != backStackEntry) return@LaunchedEffect

		withContext(Dispatchers.Main.immediate) {
			backStackEntry.savedStateHandle
				.getStateFlow<String?>(key, null)
				.flowWithLifecycle(backStackEntry.lifecycle, minActiveState)
				.collect { serializedResult ->
					if (serializedResult == null) return@collect

					val result = runCatching {
						navigationResultJson.decodeFromString<T>(serializedResult)
					}.getOrElse { throwable ->
						navigationResultLogger.w(throwable) { "Failed to decode back result '$key'." }
						backStackEntry.savedStateHandle[key] = null
						return@collect
					}

					if (awaitFrame) withFrameNanos { }

					onResult(result)
					backStackEntry.savedStateHandle[key] = null
				}
		}
	}
}
