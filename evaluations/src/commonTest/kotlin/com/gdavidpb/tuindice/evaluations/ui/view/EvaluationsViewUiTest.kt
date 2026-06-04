package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.testing.evaluationsGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationsViewUiTest {
	@Test
	fun when_gradeButtonTapped_then_invokesEvaluationClickCallback() = runTuIndiceUiTest {
			val groups = evaluationsGroupItemsFixture()
			var clickedEvaluationId: String? = null
			var clickedEvaluationName: String? = null
			var clickedSubjectCode: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = evaluationsWeekGroupItemsFixture(groups),
				selectedWeekNumber = 8,
				onVisibleWeekChange = {},
					onEvaluationClick = { evaluationId, evaluationName, subjectCode ->
						clickedEvaluationId = evaluationId
						clickedEvaluationName = evaluationName
						clickedSubjectCode = subjectCode
					},
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		val item = groups.first().items.first()

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(8))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		assertNodeVisible(EvaluationsUiTags.evaluationHeader(groups.first().title))

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton).performClick()

			assertEquals(item.evaluationId, clickedEvaluationId)
			assertEquals(item.nameText, clickedEvaluationName)
			assertEquals(item.subjectCodeText, clickedSubjectCode)
	}

	@Test
	fun when_itemIsNotClickable_then_gradeButtonTapDoesNotInvokeEvaluationCallback() = runTuIndiceUiTest {
		val sourceGroups = evaluationsGroupItemsFixture()
		val disabledItem = sourceGroups.first().items.first().copy(
			evaluationId = "evaluation-item-disabled",
			isClickable = false
		)
		val groups = listOf(
			sourceGroups.first().copy(items = listOf(disabledItem))
		)
		var clickedEvaluationId: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = evaluationsWeekGroupItemsFixture(groups),
				selectedWeekNumber = 8,
				onVisibleWeekChange = {},
				onEvaluationClick = { evaluationId, _, _ -> clickedEvaluationId = evaluationId },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeActionButton).performClick()

		assertEquals(null, clickedEvaluationId)
	}
}
