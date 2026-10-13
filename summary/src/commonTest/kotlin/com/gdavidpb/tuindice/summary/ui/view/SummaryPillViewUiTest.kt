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
class SummaryPillViewUiTest {
	@Test
	fun when_pillIsTheAccentOne_then_itSaysEnCurso() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPillView(text = "En curso", isAccent = true)
		}

		onNodeWithText("En curso").assertIsDisplayed()
	}

	@Test
	fun when_pillIsTheQuietOne_then_itSaysTheDay() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPillView(text = "Lun 19", isAccent = false)
		}

		onNodeWithText("Lun 19").assertIsDisplayed()
	}

	@Test
	fun when_pillHasShortText_then_itIsAsWideAsTheLeadColumn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryPillView(modifier = Modifier.testTag("pill"), text = "Hoy", isAccent = false)
		}

		onNodeWithTag("pill").assertWidthIsAtLeast(64.dp)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theTextIsWholeOnOneLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryPillView(text = "En curso", isAccent = true)
			}
		}

		assertTextIsWhole("En curso", singleLine = true)
	}
}
