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

		setTuIndiceTestContent {
			EvaluationHeaderView(label = label)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationHeader(label))
		onNodeWithText(label).assertIsDisplayed()
	}

	@Test
	fun when_labelHasSpecialCharacters_then_headerTagStillResolves() = runTuIndiceUiTest {
		val label = "Semana 1 / 2026"

		setTuIndiceTestContent {
			EvaluationHeaderView(label = label)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationHeader(label))
		onNodeWithText(label).assertIsDisplayed()
	}
}
