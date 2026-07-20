package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator

/**
 * ViewModelStore per entry with navigator-decided lifetime: stores live in
 * [TuIndiceNavigator.entryStores] so parked tabs keep their machines alive;
 * clearing happens on real removal (pop or root swap), never on leaving the
 * displayed back stack.
 */
@Composable
fun rememberTuIndiceRetainedViewModelStoreDecorator(navigator: TuIndiceNavigator): NavEntryDecorator<NavKey> =
	remember(navigator) {
		NavEntryDecorator { entry ->
			val storeOwner = navigator.entryStores.viewModelStoreOwner(
				storeKey = entry.contentKey.toString()
			)

			CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
				entry.Content()
			}
		}
	}
