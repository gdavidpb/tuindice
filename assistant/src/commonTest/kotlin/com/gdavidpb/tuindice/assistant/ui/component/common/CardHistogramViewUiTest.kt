package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.model.HistogramBarItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CardHistogramViewUiTest {
	@Test
	fun when_aRowIsShown_then_itReadsAsOneSentence_andItsBarIsDecoration() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardHistogramView(bars = listOf(bar(grade = "4", count = "16", fraction = 1f, sentence = "Nota 4: 16 materias")))
		}
		waitForIdle()

		onNodeWithContentDescription("Nota 4: 16 materias").assertIsDisplayed()
		// The grade and the count are read through the sentence, so they are not separate stops for the reader.
		onNodeWithText("16").assertDoesNotExist()
	}

	@Test
	fun when_theGradesAreShown_then_eachGradeHasItsOwnRowInOrder() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardHistogramView(
				bars = listOf(
					bar(grade = "1", count = "0", fraction = 0f, sentence = "Nota 1: 0 materias"),
					bar(grade = "2", count = "3", fraction = 0.25f, sentence = "Nota 2: 3 materias"),
					bar(grade = "3", count = "12", fraction = 1f, sentence = "Nota 3: 12 materias")
				)
			)
		}
		waitForIdle()

		onAllNodesWithContentDescription("Nota ", substring = true).assertCountEquals(3)
		onNodeWithContentDescription("Nota 2: 3 materias").assertIsDisplayed()
	}

	private fun bar(grade: String, count: String, fraction: Float, sentence: String) =
		HistogramBarItem(grade = grade, count = count, fraction = fraction, description = UiText.Raw(sentence))
}
