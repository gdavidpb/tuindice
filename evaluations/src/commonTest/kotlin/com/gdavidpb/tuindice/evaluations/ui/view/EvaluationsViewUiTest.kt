package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.testing.evaluationsGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationsViewUiTest {
	@Test
	fun when_itemTapped_then_invokesEvaluationClickCallback() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		var clickedEvaluationId: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				evaluations = groups,
				onEvaluationClick = { evaluationId -> clickedEvaluationId = evaluationId },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		val item = groups.first().items.first()

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		assertNodeVisible(EvaluationsUiTags.evaluationHeader(groups.first().title))

		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId)).performClick()

		assertEquals(item.evaluationId, clickedEvaluationId)
	}

	@Test
	fun when_itemIsNotClickable_then_tapDoesNotInvokeEvaluationCallback() = runTuIndiceUiTest {
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
				evaluations = groups,
				onEvaluationClick = { evaluationId -> clickedEvaluationId = evaluationId },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(disabledItem.evaluationId)).performClick()

		assertEquals(null, clickedEvaluationId)
	}
}
