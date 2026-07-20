package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.LocalNavEntryScope
import com.gdavidpb.tuindice.base.presentation.navigation.NavEntryScope
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator

/**
 * Provides [LocalNavEntryScope] so base collectors (nav results, current-entry
 * hoisting) can gate on whether their entry is the top of the back stack.
 */
@Composable
fun rememberTuIndiceEntryScopeDecorator(navigator: TuIndiceNavigator): NavEntryDecorator<NavKey> =
	remember(navigator) {
		NavEntryDecorator { entry ->
			val storeKey = entry.contentKey.toString()
			val isCurrent = remember(storeKey) {
				derivedStateOf { navigator.currentStoreKey == storeKey }
			}
			val entryScope = remember(storeKey) {
				NavEntryScope(
					storeKey = storeKey,
					isCurrent = isCurrent,
					resultStore = navigator.resultStore
				)
			}

			CompositionLocalProvider(LocalNavEntryScope provides entryScope) {
				entry.Content()
			}
		}
	}
