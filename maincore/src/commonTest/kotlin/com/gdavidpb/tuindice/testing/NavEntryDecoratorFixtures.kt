package com.gdavidpb.tuindice.testing

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator

/**
 * Smallest host that runs one entry decorator over the navigator's back stack, with the
 * tab-scoped content key the app host gives each entry. By default only the top entry is
 * composed, so an entry parked below it leaves composition as it does under a full-screen
 * scene; [composeParkedEntries] keeps the whole stack composed, as a dialog scene does.
 */
@Composable
fun DecoratedNavEntries(
	navigator: TuIndiceNavigator,
	decorator: NavEntryDecorator<NavKey>,
	composeParkedEntries: Boolean = false,
	content: @Composable (key: NavKey) -> Unit
) {
	val entries = rememberDecoratedNavEntries(
		backStack = navigator.backStack,
		entryDecorators = remember(decorator) { listOf(decorator) },
		entryProvider = { key ->
			NavEntry(
				key = key,
				contentKey = navigator.storeKeyOf(key),
				content = { entryKey -> content(entryKey) }
			)
		}
	)
	val composedEntries = if (composeParkedEntries) entries else entries.takeLast(1)

	Column {
		composedEntries.forEach { entry ->
			key(entry.contentKey) {
				entry.Content()
			}
		}
	}
}

fun NavKey.probeLabel(): String = this::class.simpleName.orEmpty()
