package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.subjects.testing.subjectDetailItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailHeaderViewUiTest {
	@Test
	fun when_gradingModeTextIsNull_then_displaysNameCodeAndCreditsOnly() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailHeaderView(
				detail = subjectDetailItem().copy(
					id = "MAT101",
					name = "Calculo I",
					creditsText = "5 UC",
					gradingModeText = null
				)
			)
		}

		onNodeWithText("Calculo I").assertIsDisplayed()
		onNodeWithText("MAT101").assertIsDisplayed()
		onNodeWithText("5 UC").assertIsDisplayed()
		onAllNodesWithText("Cualitativa").assertCountEquals(0)
	}

	@Test
	fun when_gradingModeTextProvided_then_displaysGradingModeNextToCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailHeaderView(
				detail = subjectDetailItem().copy(
					id = "EP1420",
					name = "Pasantia",
					creditsText = "3 UC",
					gradingModeText = "Cualitativa"
				)
			)
		}

		onNodeWithText("Pasantia").assertIsDisplayed()
		onNodeWithText("EP1420").assertIsDisplayed()
		onNodeWithText("3 UC").assertIsDisplayed()
		onNodeWithText("Cualitativa").assertIsDisplayed()
	}
}
