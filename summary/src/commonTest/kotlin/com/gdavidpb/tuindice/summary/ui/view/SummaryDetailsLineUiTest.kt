package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryDetailsLineUiTest {
	private val place = ClassPlaceItem(
		room = "MYS-210",
		scheduleText = UiText.Raw("3-4 · 9:30–11:30 a. m.")
	)

	@Test
	fun when_thePlaceIsGiven_then_theRoomIsOnItsOwnLine_andTheScheduleUnderIt() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Expanded) {
			Box(modifier = Modifier.width(600.dp)) {
				SummaryDetailsLine(place = place)
			}
		}

		val room = onNodeWithText("MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val schedule = onNodeWithText("3-4 · 9:30–11:30 a. m.", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(schedule.top >= room.bottom, "the schedule goes under the room")
		assertTrue(abs(schedule.left - room.left) <= 1f, "both lines start at the same edge")
	}

	@Test
	fun when_theLetterIsLargeOnANarrowLine_then_nothingIsCut() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(160.dp)) {
					SummaryDetailsLine(place = place)
				}
			}
		}

		assertTextIsWhole("MYS-210", singleLine = true)
		assertTextIsWhole("3-4 · 9:30–11:30 a. m.")
	}

	@Test
	fun when_theSubjectHasNoClassThatDay_then_nothingIsDrawnUnderTheTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(200.dp)) {
					SummaryDetailsLine(place = null)
				}
			}
		}

		onNodeWithText("MYS-210", useUnmergedTree = true).assertDoesNotExist()
		onNodeWithText("3-4 · 9:30–11:30 a. m.", useUnmergedTree = true).assertDoesNotExist()
	}
}
