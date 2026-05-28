package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

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
		onNodeWithText(item.nameText).assertIsDisplayed()
		onNodeWithText(item.subjectCodeText).assertIsDisplayed()
		onNodeWithText(item.dateText).assertIsDisplayed()
		onNodeWithText(item.typeText).assertIsDisplayed()
		onNodeWithText(item.gradeActionText).assertIsDisplayed()
		onNodeWithTag(
			testTag = EvaluationsUiTags.EvaluationTypeLeadingIcon,
			useUnmergedTree = true
		).assertIsDisplayed()
		assertNodeVisible(EvaluationsUiTags.EvaluationGradeActionButton)
	}

	@Test
	fun when_gradeActionHidden_then_doesNotDisplayGradeButton() = runTuIndiceUiTest {
		val item = evaluationItemFixture(showsGradeAction = false)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		assertNodeHidden(EvaluationsUiTags.EvaluationGradeActionButton)
	}

	@Test
	fun when_gradeButtonTapped_then_invokesGradeCallback() = runTuIndiceUiTest {
		val item = evaluationItemFixture()
		var gradeClicks = 0

		setTuIndiceTestContent {
			EvaluationItemView(
				item = item,
				onGradeClick = { gradeClicks++ }
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton).performClick()

		assertEquals(1, gradeClicks)
	}

	@Test
	fun when_cardTapped_then_invokesCardCallback() = runTuIndiceUiTest {
		val item = evaluationItemFixture()
		var cardClicks = 0

		setTuIndiceTestContent {
			EvaluationItemView(
				item = item,
				onCardClick = { cardClicks++ }
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId)).performClick()

		assertEquals(1, cardClicks)
	}
}
