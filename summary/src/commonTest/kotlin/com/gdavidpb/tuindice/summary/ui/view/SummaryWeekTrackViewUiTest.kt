package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryWeekTrackViewUiTest {
	@Test
	fun when_trackIsShown_then_itIsOneElementThatSaysTheWeek() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekTrackView(
				modifier = Modifier.testTag("track"),
				week = 3,
				totalWeeks = 12,
				description = "Semana 3 de 12"
			)
		}

		onNodeWithTag("track").assertIsDisplayed()
		onNodeWithTag("track").assertContentDescriptionEquals("Semana 3 de 12")
	}

	@Test
	fun when_trackIsShown_then_itIsAsWideAsItsParentAndASegmentTall() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekTrackView(
				modifier = Modifier.testTag("track"),
				week = 12,
				totalWeeks = 12,
				description = "Semana 12 de 12"
			)
		}

		onNodeWithTag("track").assertWidthIsEqualTo(360.dp)
		onNodeWithTag("track").assertHeightIsEqualTo(TuIndiceComponentSizes.ProgressBarHeight)
	}

	@Test
	fun when_weekIsTheFirst_then_itStillRendersAllTheSegments() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekTrackView(
				modifier = Modifier.testTag("track"),
				week = 1,
				totalWeeks = 12,
				description = "Semana 1 de 12"
			)
		}

		onNodeWithTag("track").assertContentDescriptionEquals("Semana 1 de 12")
		onNodeWithTag("track").assertWidthIsEqualTo(360.dp)
	}
}
