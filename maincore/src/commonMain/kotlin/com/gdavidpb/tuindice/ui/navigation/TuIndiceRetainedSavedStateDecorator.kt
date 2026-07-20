package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator

/**
 * Saveable-state per entry with navigator-decided lifetime: state is removed
 * only when the navigator reports a real removal from the owning stack, so
 * parked tabs keep their rememberSaveable state.
 */
@Composable
fun rememberTuIndiceRetainedSavedStateDecorator(navigator: TuIndiceNavigator): NavEntryDecorator<NavKey> {
	val saveableStateHolder = rememberSaveableStateHolder()

	DisposableEffect(navigator, saveableStateHolder) {
		val unregister = navigator.entryStores.addRemovalListener { storeKey ->
			saveableStateHolder.removeState(storeKey)
		}

		onDispose { unregister() }
	}

	return remember(navigator, saveableStateHolder) {
		NavEntryDecorator { entry ->
			saveableStateHolder.SaveableStateProvider(key = entry.contentKey.toString()) {
				entry.Content()
			}
		}
	}
}
