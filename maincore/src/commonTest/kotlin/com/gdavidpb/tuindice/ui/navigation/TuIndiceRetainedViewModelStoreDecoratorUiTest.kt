package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.DecoratedNavEntries
import com.gdavidpb.tuindice.testing.probeLabel
import com.gdavidpb.tuindice.testing.rememberTestNavigator
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * The decorator alone, over a probe that asks for a ViewModel the way a route does. Each
 * ViewModel carries the order it was built in and reports when it is cleared, so a kept
 * instance, a rebuilt one and a cleared one read differently. Only the top entry is composed.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceRetainedViewModelStoreDecoratorUiTest {
	@Test
	fun when_entryIsDecorated_then_itsOwnerIsTheNavigatorStoreForItsKey() = runTuIndiceUiTest {
		val probe = ViewModelProbe()
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ViewModelEntries(navigator = navigator, probe = probe)
		}

		runOnIdle { navigator.push(BROWSER) }

		runOnIdle {
			val summaryOwner = probe.owners.getValue(SummaryDestination.Summary)
			val browserOwner = probe.owners.getValue(BROWSER)

			assertSame(
				navigator.entryStores.viewModelStoreOwner("SUMMARY:${SummaryDestination.Summary}"),
				summaryOwner
			)
			assertSame(navigator.entryStores.viewModelStoreOwner("SUMMARY:$BROWSER"), browserOwner)
			assertNotSame(summaryOwner, browserOwner)
		}
	}

	@Test
	fun when_entryIsParkedUnderAnotherOne_then_itsViewModelIsKeptAlive() = runTuIndiceUiTest {
		val probe = ViewModelProbe()
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ViewModelEntries(navigator = navigator, probe = probe)
		}

		onNodeWithText("Summary: vm#1").assertExists()

		runOnIdle { navigator.push(BROWSER) }
		onNodeWithText("Browser: vm#2").assertExists()
		onNodeWithText("Summary: vm#1").assertDoesNotExist()

		runOnIdle { assertEquals(emptyList(), probe.cleared) }

		runOnIdle { navigator.pop() }
		onNodeWithText("Summary: vm#1").assertExists()

		runOnIdle { assertEquals(2, probe.builds) }
	}

	@Test
	fun when_entryIsPopped_then_itsViewModelIsClearedAndRebuiltOnReturn() = runTuIndiceUiTest {
		val probe = ViewModelProbe()
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ViewModelEntries(navigator = navigator, probe = probe)
		}

		runOnIdle { navigator.push(BROWSER) }
		onNodeWithText("Browser: vm#2").assertExists()

		runOnIdle { navigator.pop() }
		onNodeWithText("Summary: vm#1").assertExists()
		runOnIdle { assertEquals(listOf(2), probe.cleared) }

		runOnIdle { navigator.push(BROWSER) }
		onNodeWithText("Browser: vm#3").assertExists()
		runOnIdle { assertEquals(listOf(2), probe.cleared) }
	}

	@Test
	fun when_tabIsParked_then_bothTabsKeepTheirViewModels() = runTuIndiceUiTest {
		val probe = ViewModelProbe()
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			ViewModelEntries(navigator = navigator, probe = probe)
		}

		onNodeWithText("Summary: vm#1").assertExists()

		runOnIdle { navigator.switchTab(MainSection.RECORD) }
		onNodeWithText("Record: vm#2").assertExists()

		runOnIdle { navigator.switchTab(MainSection.SUMMARY) }
		onNodeWithText("Summary: vm#1").assertExists()

		runOnIdle { navigator.switchTab(MainSection.RECORD) }
		onNodeWithText("Record: vm#2").assertExists()

		runOnIdle {
			assertEquals(2, probe.builds)
			assertEquals(emptyList(), probe.cleared)
		}
	}

	@Composable
	private fun ViewModelEntries(
		navigator: TuIndiceNavigator,
		probe: ViewModelProbe
	) {
		DecoratedNavEntries(
			navigator = navigator,
			decorator = rememberTuIndiceRetainedViewModelStoreDecorator(navigator = navigator)
		) { key ->
			val owner = checkNotNull(LocalViewModelStoreOwner.current)
			val viewModel = viewModel { probe.build() }

			SideEffect { probe.owners[key] = owner }

			Text(text = "${key.probeLabel()}: vm#${viewModel.order}")
		}
	}

	private class ViewModelProbe {
		val owners = mutableMapOf<NavKey, ViewModelStoreOwner>()
		val cleared = mutableListOf<Int>()
		var builds = 0
			private set

		fun build(): ProbeViewModel = ProbeViewModel(order = ++builds, onClear = cleared::add)
	}

	private class ProbeViewModel(
		val order: Int,
		private val onClear: (order: Int) -> Unit
	) : ViewModel() {
		override fun onCleared() {
			onClear(order)
		}
	}

	private companion object {
		val BROWSER = BrowserDestination.Browser(
			title = "Privacidad",
			url = "https://tuindice.app/privacy"
		)
	}
}
