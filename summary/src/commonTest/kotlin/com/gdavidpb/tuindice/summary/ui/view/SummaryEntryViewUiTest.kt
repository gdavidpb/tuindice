package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryEntryViewUiTest {
	private val place = ClassPlaceItem(
		placeText = UiText.Raw("3-4 · MYS-210"),
		timeText = UiText.Raw("9:30–11:30 a. m.")
	)

	@Test
	fun when_entryHasTitleAndPlace_then_itShowsTitleChipAndBlocksFirstThenTheHour() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEntryView(titleText = "Parcial 1", subjectCode = "CI5437", place = place)
		}

		onNodeWithText("Parcial 1").assertIsDisplayed()
		onNodeWithText("CI5437").assertIsDisplayed()
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("9:30–11:30 a. m.", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_entryHasNoTitleAndNoPlace_then_itShowsOnlyTheChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEntryView(titleText = null, subjectCode = "EG1114", place = null)
		}

		onNodeWithText("EG1114").assertIsDisplayed()
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_theLetterIsLargeAndTheTitleLong_then_theChipGoesDownInsteadOfBeingSqueezed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(200.dp)) {
					SummaryEntryView(titleText = "Trabajo escrito 12", subjectCode = "CI5437", place = place)
				}
			}
		}

		assertTextIsWhole("Trabajo escrito 12")
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("3-4 · MYS-210")
		assertTextIsWhole("9:30–11:30 a. m.", singleLine = true)
	}
}
