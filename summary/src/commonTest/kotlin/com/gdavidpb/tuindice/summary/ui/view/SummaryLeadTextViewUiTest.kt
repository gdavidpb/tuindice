package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryLeadTextViewUiTest {
	@Test
	fun when_leadIsShown_then_itSaysTheDay() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLeadTextView(text = "Vie 16")
		}

		onNodeWithText("Vie 16").assertIsDisplayed()
	}

	@Test
	fun when_leadIsShort_then_itKeepsTheWidthOfTheLeadColumn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLeadTextView(modifier = Modifier.testTag("lead"), text = "Hoy")
		}

		onNodeWithTag("lead").assertWidthIsAtLeast(64.dp)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theDayIsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryLeadTextView(text = "Mié 28")
			}
		}

		assertTextIsWhole("Mié 28", singleLine = true)
	}
}
