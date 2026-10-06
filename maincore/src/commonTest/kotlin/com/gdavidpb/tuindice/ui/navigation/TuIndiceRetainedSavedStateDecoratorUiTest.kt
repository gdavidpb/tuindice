package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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

/**
 * The decorator alone, over a probe that counts taps in a rememberSaveable. Only the top
 * entry is composed, so a parked entry really leaves composition: whatever it shows when it
 * comes back is what the decorator kept for it.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceRetainedSavedStateDecoratorUiTest {
	@Test
	fun when_entryIsParkedUnderAnotherOne_then_itsSaveableStateSurvives() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			CountingEntries(navigator = navigator)
		}

		tapCounter(times = 2)
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Summary: 2")

		runOnIdle { navigator.push(BROWSER) }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Browser: 0")

		runOnIdle { navigator.pop() }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Summary: 2")
	}

	@Test
	fun when_entryIsPopped_then_itsSaveableStateIsDiscarded() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			CountingEntries(navigator = navigator)
		}

		tapCounter(times = 2)

		runOnIdle { navigator.push(BROWSER) }
		tapCounter(times = 3)
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Browser: 3")

		runOnIdle { navigator.pop() }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Summary: 2")

		// Same destination and store key as before, while the entry under it kept its count:
		// only the removal the navigator reported explains starting over.
		runOnIdle { navigator.push(BROWSER) }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Browser: 0")
	}

	@Test
	fun when_tabIsParked_then_itsSaveableStateSurvivesTheSwitch() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		setTuIndiceTestContent {
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

			CountingEntries(navigator = navigator)
		}

		tapCounter(times = 1)

		runOnIdle { navigator.switchTab(MainSection.RECORD) }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Record: 0")
		tapCounter(times = 4)

		runOnIdle { navigator.switchTab(MainSection.SUMMARY) }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Summary: 1")

		runOnIdle { navigator.switchTab(MainSection.RECORD) }
		onNodeWithTag(COUNTER_TAG).assertTextEquals("Record: 4")
	}

	@Composable
	private fun CountingEntries(navigator: TuIndiceNavigator) {
		DecoratedNavEntries(
			navigator = navigator,
			decorator = rememberTuIndiceRetainedSavedStateDecorator(navigator = navigator)
		) { key ->
			var count by rememberSaveable { mutableStateOf(0) }

			Text(
				modifier = Modifier
					.testTag(COUNTER_TAG)
					.clickable { count++ },
				text = "${key.probeLabel()}: $count"
			)
		}
	}

	private fun ComposeUiTest.tapCounter(times: Int) {
		repeat(times) {
			onNodeWithTag(COUNTER_TAG).performClick()
		}
	}

	private companion object {
		const val COUNTER_TAG = "saved_state_probe_counter"

		val BROWSER = BrowserDestination.Browser(
			title = "Privacidad",
			url = "https://tuindice.app/privacy"
		)
	}
}
