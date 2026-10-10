package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.LocalNavEntryScope
import com.gdavidpb.tuindice.base.presentation.navigation.NavEntryScope
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.DecoratedNavEntries
import com.gdavidpb.tuindice.testing.probeLabel
import com.gdavidpb.tuindice.testing.rememberTestNavigator
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * The decorator alone, over a probe that reads [LocalNavEntryScope]: which scope each entry
 * gets and when it reports itself as the current one. The whole stack stays composed, as it
 * does under a dialog, because that is the case the gate exists for.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceEntryScopeDecoratorUiTest {
	@Test
	fun when_entryIsDecorated_then_scopeCarriesItsStoreKeyAndTheNavigatorResultStore() = runTuIndiceUiTest {
		val scopes = mutableMapOf<NavKey, NavEntryScope>()
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ScopedEntries(navigator = navigator, scopes = scopes)
		}

		runOnIdle { navigator.push(BROWSER) }

		runOnIdle {
			val summaryScope = scopes.getValue(SummaryDestination.Summary)
			val browserScope = scopes.getValue(BROWSER)

			assertEquals("SUMMARY:${SummaryDestination.Summary}", summaryScope.storeKey)
			assertEquals("SUMMARY:$BROWSER", browserScope.storeKey)
			assertSame(navigator.resultStore, summaryScope.resultStore)
			assertSame(navigator.resultStore, browserScope.resultStore)
		}
	}

	@Test
	fun when_anotherEntryIsPushedOnTop_then_onlyTheTopEntryIsCurrent() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ScopedEntries(navigator = navigator)
		}

		onNodeWithText("Summary current=true").assertExists()

		runOnIdle { navigator.push(BROWSER) }

		onNodeWithText("Summary current=false").assertExists()
		onNodeWithText("Browser current=true").assertExists()

		runOnIdle { navigator.pop() }

		onNodeWithText("Summary current=true").assertExists()
		onNodeWithText("Browser current=true").assertDoesNotExist()
	}

	@Test
	fun when_tabIsSwitched_then_startTabEntryKeepsItsScopeAndStopsBeingCurrent() = runTuIndiceUiTest {
		val scopes = mutableMapOf<NavKey, NavEntryScope>()
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ScopedEntries(navigator = navigator, scopes = scopes)
		}

		val summaryScope = runOnIdle { scopes.getValue(SummaryDestination.Summary) }

		runOnIdle { navigator.switchTab(MainSection.RECORD) }

		// The start tab stays under every other tab, so its entry is composed but not on top.
		onNodeWithText("Summary current=false").assertExists()
		onNodeWithText("Record current=true").assertExists()

		runOnIdle {
			assertEquals("RECORD:${RecordDestination.Record}", scopes.getValue(RecordDestination.Record).storeKey)
			assertSame(summaryScope, scopes.getValue(SummaryDestination.Summary))
		}

		runOnIdle { navigator.switchTab(MainSection.SUMMARY) }

		onNodeWithText("Summary current=true").assertExists()
		runOnIdle { assertSame(summaryScope, scopes.getValue(SummaryDestination.Summary)) }
	}

	@Composable
	private fun ScopedEntries(
		navigator: TuIndiceNavigator,
		scopes: MutableMap<NavKey, NavEntryScope> = mutableMapOf()
	) {
		DecoratedNavEntries(
			navigator = navigator,
			decorator = rememberTuIndiceEntryScopeDecorator(navigator = navigator),
			composeParkedEntries = true
		) { key ->
			val scope = LocalNavEntryScope.current

			SideEffect { scopes[key] = scope }

			Text(text = "${key.probeLabel()} current=${scope.isCurrent.value}")
		}
	}

	private companion object {
		val BROWSER = BrowserDestination.Browser(
			title = "Privacidad",
			url = "https://tuindice.app/privacy"
		)
	}
}
