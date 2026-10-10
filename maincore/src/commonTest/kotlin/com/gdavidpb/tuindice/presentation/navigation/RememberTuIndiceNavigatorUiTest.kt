package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * What the composable adds over the navigator it builds: how a start key becomes a root mode,
 * a start tab and a first stack, that the retention stores come from the host-scoped
 * ViewModel, and that all of it is remembered and saved rather than rebuilt.
 */
@OptIn(ExperimentalTestApi::class)
class RememberTuIndiceNavigatorUiTest {
	@Test
	fun when_startKeyIsSignIn_then_startsInAuthModeOnTheAuthStack() = runTuIndiceUiTest {
		val navigator = composeNavigator(startKey = AuthDestination.SignIn)

		runOnIdle {
			assertEquals(TuIndiceRootMode.AUTH, navigator().rootMode)
			assertEquals(listOf<Destination>(AuthDestination.SignIn), navigator().backStack)
			assertEquals(MainSection.SUMMARY, navigator().startTab)
		}
	}

	@Test
	fun when_startKeyIsATabRoot_then_startsSignedInOnThatTab() = runTuIndiceUiTest {
		val navigator = composeNavigator(startKey = RecordDestination.Record)

		runOnIdle {
			assertEquals(TuIndiceRootMode.SIGNED_IN, navigator().rootMode)
			assertEquals(MainSection.RECORD, navigator().startTab)
			assertEquals(MainSection.RECORD, navigator().currentTab)
			assertEquals(listOf<Destination>(RecordDestination.Record), navigator().backStack)
		}
	}

	@Test
	fun when_startKeyIsNotATabRoot_then_pushesItOverTheSummaryTab() = runTuIndiceUiTest {
		val navigator = composeNavigator(startKey = BROWSER)

		runOnIdle {
			assertEquals(TuIndiceRootMode.SIGNED_IN, navigator().rootMode)
			assertEquals(MainSection.SUMMARY, navigator().startTab)
			assertEquals(MainSection.SUMMARY, navigator().currentTab)
			assertEquals(listOf(SummaryDestination.Summary, BROWSER), navigator().backStack)
		}
	}

	@Test
	fun when_recomposed_then_keepsTheNavigatorAndDoesNotReapplyTheStartKey() = runTuIndiceUiTest {
		val stores = NavEntryStoresViewModel()
		val seen = mutableListOf<TuIndiceNavigator>()
		var tick by mutableStateOf(0)

		setTuIndiceTestContent {
			val navigator = rememberTuIndiceNavigator(startKey = BROWSER, storesViewModel = stores)

			seen += navigator
			Text(text = "tick $tick")
		}

		runOnIdle {
			seen.last().pop()
			tick++
		}
		onNodeWithText("tick 1").assertExists()

		runOnIdle {
			assertTrue(seen.size >= 2)
			assertTrue(seen.all { navigator -> navigator === seen.first() })
			assertEquals(listOf<Destination>(SummaryDestination.Summary), seen.last().backStack)
			assertSame(stores.entryStores, seen.last().entryStores)
			assertSame(stores.resultStore, seen.last().resultStore)
		}
	}

	@Test
	fun when_stateIsRestored_then_keepsStacksAndTabWithoutReapplyingTheStartKey() = runTuIndiceUiTest {
		val stores = NavEntryStoresViewModel()
		val host = RestorableHost()
		var latest: TuIndiceNavigator? = null

		setTuIndiceTestContent {
			host.Content {
				latest = rememberTuIndiceNavigator(startKey = BROWSER, storesViewModel = stores)
			}
		}

		val first = runOnIdle {
			checkNotNull(latest).also { navigator ->
				navigator.pop()
				navigator.switchTab(MainSection.RECORD)
				navigator.push(RecordDestination.CreateSyntheticTerm(termId = null))
			}
		}

		runOnIdle { host.saveAndLeaveComposition() }
		runOnIdle { host.restore() }

		runOnIdle {
			val restored = checkNotNull(latest)

			assertNotSame(first, restored)
			assertEquals(TuIndiceRootMode.SIGNED_IN, restored.rootMode)
			assertEquals(MainSection.SUMMARY, restored.startTab)
			assertEquals(MainSection.RECORD, restored.currentTab)
			// The start key was popped before saving: restoring must not push it back.
			assertEquals(
				listOf(
					SummaryDestination.Summary,
					RecordDestination.Record,
					RecordDestination.CreateSyntheticTerm(termId = null)
				),
				restored.backStack
			)
			assertSame(stores.entryStores, restored.entryStores)
		}
	}

	private fun ComposeUiTest.composeNavigator(
		startKey: Destination
	): () -> TuIndiceNavigator {
		var navigator: TuIndiceNavigator? = null

		setTuIndiceTestContent {
			navigator = rememberTuIndiceNavigator(
				startKey = startKey,
				storesViewModel = NavEntryStoresViewModel()
			)
		}

		return { checkNotNull(navigator) }
	}

	/**
	 * Stands in for the platform's save and restore: the content leaves composition after its
	 * saveable state is collected, and comes back at the same position over a registry that
	 * holds only what was saved.
	 */
	private class RestorableHost {
		private var registry by mutableStateOf(SaveableStateRegistry(restoredValues = null) { true })
		private var isComposed by mutableStateOf(true)
		private var saved: Map<String, List<Any?>> = emptyMap()

		@Composable
		fun Content(content: @Composable () -> Unit) {
			if (isComposed) {
				CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
					content()
				}
			}
		}

		fun saveAndLeaveComposition() {
			saved = registry.performSave()
			isComposed = false
		}

		fun restore() {
			registry = SaveableStateRegistry(restoredValues = saved) { true }
			isComposed = true
		}
	}

	private companion object {
		val BROWSER = BrowserDestination.Browser(
			title = "Privacidad",
			url = "https://tuindice.app/privacy"
		)
	}
}
