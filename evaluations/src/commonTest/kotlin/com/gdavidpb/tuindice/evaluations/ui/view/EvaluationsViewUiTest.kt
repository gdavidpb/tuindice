package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.testing.evaluationsGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
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
		assertNodeVisible(EvaluationsUiTags.evaluationHeader("Semana 8"))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		assertNodeHidden(EvaluationsUiTags.evaluationHeader(groups.first().title))
		onNodeWithText("1 evaluación").assertIsDisplayed()

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

	@Test
	fun when_weekHasNoEvaluations_then_hidesWeekHeader() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				weekNumber = 7,
				title = "Semana 7",
				groups = emptyList()
			),
			evaluationsWeekGroupItemsFixture(groups).first()
		)

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weekGroups,
				selectedWeekNumber = 7,
				onVisibleWeekChange = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(7))
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(8))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(groups.first().items.first().evaluationId))
	}
}
