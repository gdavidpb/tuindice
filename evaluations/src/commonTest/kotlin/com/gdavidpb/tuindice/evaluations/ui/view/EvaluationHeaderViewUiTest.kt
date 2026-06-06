package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationHeaderViewUiTest {
	@Test
	fun when_rendered_then_displaysLabel() = runTuIndiceUiTest {
		val label = "Esta semana"
		val count = "2 evaluaciones"

		setTuIndiceTestContent {
			EvaluationHeaderView(
				label = label,
				countText = count
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationHeader(label))
		onNodeWithText(label).assertIsDisplayed()
		onNodeWithText(count).assertIsDisplayed()
	}

	@Test
	fun when_labelHasSpecialCharacters_then_headerTagStillResolves() = runTuIndiceUiTest {
		val label = "Semana 1 / 2026"
		val count = "1 evaluación"

		setTuIndiceTestContent {
			EvaluationHeaderView(
				label = label,
				countText = count
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationHeader(label))
		onNodeWithText(label).assertIsDisplayed()
		onNodeWithText(count).assertIsDisplayed()
	}
}
