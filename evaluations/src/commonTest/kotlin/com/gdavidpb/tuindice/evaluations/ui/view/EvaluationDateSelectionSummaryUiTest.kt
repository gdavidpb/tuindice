package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationDateSelectionSummaryUiTest {
	@Test
	fun when_theDayHasClass_then_theDateIsFollowedByWhereTheClassIs() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDateSelectionSummary(
				dateText = UiText.Raw("Miércoles — 21/10/26"),
				classPlace = ClassPlaceItem(
					placeText = UiText.Raw("En clase · Bloques 1-2 · MYS-116"),
					timeText = UiText.Raw("7:30–9:30 a. m.")
				)
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSelection)
		onNodeWithText("Miércoles — 21/10/26").assertIsDisplayed()
		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSelectionPlace)
		onNodeWithText("En clase · Bloques 1-2 · MYS-116").assertIsDisplayed()
		onNodeWithText("7:30–9:30 a. m.").assertIsDisplayed()
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
