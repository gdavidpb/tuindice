package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryClassRowItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class SummaryClassRowViewUiTest {
	@Test
	fun when_classIsInProgress_then_rowShowsEnCursoChipBlocksAndTheHourAfter() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryClassRowView(item = summaryClassRowItem(), onClick = {})
		}

		onNodeWithText("En curso", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("CI5437", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("9:30–11:30 a. m.", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_classIsTheNextOne_then_rowShowsTheDayInsteadOfEnCurso() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryClassRowView(
				item = summaryClassRowItem(isInProgress = false, leadText = "Lun 19", subjectCode = "CI5311"),
				onClick = {}
			)
		}

		onNodeWithText("Lun 19", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("CI5311", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_rowIsTapped_then_itAsksForTheSchedule_andSaysSoToAScreenReader() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryClassRowView(
				modifier = Modifier.testTag(SummaryUiTags.WeekClassRow),
				item = summaryClassRowItem(),
				onClick = { taps++ }
			)
		}

		val row = onNodeWithTag(SummaryUiTags.WeekClassRow)

		row.assertHasClickAction()
		row.assertHeightIsAtLeast(56.dp)
		assertNotNull(row.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
		assertEquals("Abrir horario", row.fetchSemanticsNode().config[SemanticsActions.OnClick].label)

		row.performClick()

		assertEquals(1, taps)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_noTextIsCutAndTheChipAndThePillAreWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryClassRowView(item = summaryClassRowItem(), onClick = {})
				}
			}
		}

		assertTextIsWhole("En curso", singleLine = true)
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("3-4 · MYS-210")
		assertTextIsWhole("9:30–11:30 a. m.", singleLine = true)
	}
}
