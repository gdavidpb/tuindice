package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationItemViewUiTest {
	@Test
	fun when_rendered_then_displaysEvaluationData() = runTuIndiceUiTest {
		val item = evaluationItemFixture(
			evaluationId = "evaluation-item-42",
			isOverdue = true
		)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		assertNodeVisible(EvaluationsUiTags.EvaluationOverdueIndicator)
		onNodeWithText(item.nameText).assertIsDisplayed()
		onNodeWithText(item.typeAndSubjectCodeText).assertIsDisplayed()
		onNodeWithText(item.gradesText).assertIsDisplayed()
	}

	@Test
	fun when_itemIsNotOverdue_then_hidesOverdueIndicator() = runTuIndiceUiTest {
		val item = evaluationItemFixture(
			evaluationId = "evaluation-item-active",
			isOverdue = false
		)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		assertNodeHidden(EvaluationsUiTags.EvaluationOverdueIndicator)
	}
}
