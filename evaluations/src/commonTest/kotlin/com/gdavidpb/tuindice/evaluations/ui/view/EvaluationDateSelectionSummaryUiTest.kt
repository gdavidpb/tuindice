package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationDateSelectionSummaryUiTest {
	@Test
	fun when_theDayHasClass_then_theDateIsFollowedByTheRoomAndTheScheduleOfTheClass() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDateSelectionSummary(
				dateText = UiText.Raw("Miércoles — 21/10/26"),
				classPlace = ClassPlaceItem(
					room = "MYS-116",
					scheduleText = UiText.Raw("En clase · 1-2 · 7:30–9:30 a. m.")
				)
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSelection)
		onNodeWithText("Miércoles — 21/10/26").assertIsDisplayed()
		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSelectionPlace)
		onNodeWithText("MYS-116").assertIsDisplayed()
		onNodeWithText("En clase · 1-2 · 7:30–9:30 a. m.").assertIsDisplayed()

		val room = onNodeWithText("MYS-116").getUnclippedBoundsInRoot()
		val schedule = onNodeWithText("En clase · 1-2 · 7:30–9:30 a. m.").getUnclippedBoundsInRoot()

		assertTrue(schedule.top >= room.bottom, "the schedule goes under the room")
	}

	@Test
	fun when_theDayHasNoClass_then_itIsOnlyTheDate() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDateSelectionSummary(
				dateText = UiText.Raw("Martes — 20/10/26"),
				classPlace = null
			)
		}

		onNodeWithText("Martes — 20/10/26").assertIsDisplayed()
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogSelectionPlace).assertDoesNotExist()
	}
}
