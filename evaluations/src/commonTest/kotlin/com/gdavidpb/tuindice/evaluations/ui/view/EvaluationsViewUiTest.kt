package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationsActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { evaluationId, evaluationName, subjectCode ->
						clickedEvaluationId = evaluationId
						clickedEvaluationName = evaluationName
						clickedSubjectCode = subjectCode
					},
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		val item = groups.first().items.first()

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(8))
		assertNodeVisible(EvaluationsUiTags.evaluationHeader("Semana 8"))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		onNodeWithText("1 evaluación").assertIsDisplayed()

		onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId)).performClick()

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
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { evaluationId, _, _ -> clickedEvaluationId = evaluationId },
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(disabledItem.evaluationId)).performClick()

		assertEquals(null, clickedEvaluationId)
	}

	@Test
	fun when_weekHasNoEvaluations_then_hidesWeekHeader() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(7),
				title = "Semana 7",
				groups = emptyList()
			),
			evaluationsWeekGroupItemsFixture(groups).first()
		)

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weekGroups,
				selectedWeekKey = EvaluationsWeekKey.Academic(7),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(7))
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(8))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(groups.first().items.first().evaluationId))
	}

	@Test
	fun when_continuousGroupExists_then_showsContinuousHeader() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		val weekGroupsFixture = groups.map { group ->
			group.copy(
				items = group.items.map { item ->
					item.copy(evaluationId = "${item.evaluationId}-week")
				}
			)
		}
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Continuous,
				title = "Continuas",
				groups = groups
			),
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(8),
				title = "Semana 8",
				groups = weekGroupsFixture
			)
		)

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weekGroups,
				selectedWeekKey = EvaluationsWeekKey.Continuous,
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(EvaluationsWeekKey.Continuous))
		assertNodeVisible(EvaluationsUiTags.evaluationHeader("Continuas"))
	}

	@Test
	fun when_arrivalIsGivenBelowTheViewport_then_itsCardIsBroughtIntoViewAndTheListTakesIt() = runTuIndiceUiTest {
		val weeks = (1..12).map { number ->
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(number),
				title = "Semana $number",
				groups = listOf(
					EvaluationsGroupItem(
						items = (1..12).map { index ->
							evaluationItemFixture(evaluationId = "week-$number-evaluation-$index")
						}
					)
				)
			)
		}
		val arrival = "week-10-evaluation-12"
		var taken: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weeks,
				selectedWeekKey = EvaluationsWeekKey.Academic(10),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {},
					onEvaluationArrivalTaken = { evaluationId -> taken = evaluationId }
				),
				arrivalEvaluationId = arrival
			)
		}

		waitForIdle()
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(arrival))
		assertEquals(arrival, taken)
	}

	@Test
	fun when_theLastWeekIsSelectedOnTheFirstLayout_then_itsHeaderReachesTheTopOfTheList() = runTuIndiceUiTest {
		val earlier = evaluationItemFixture(evaluationId = "week-7-evaluation")
		val last = evaluationItemFixture(evaluationId = "week-8-evaluation")
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(7),
				title = "Semana 7",
				groups = listOf(EvaluationsGroupItem(items = listOf(earlier)))
			),
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(8),
				title = "Semana 8",
				groups = listOf(EvaluationsGroupItem(items = listOf(last)))
			)
		)

		// The last week is far shorter than the list: only the room left at the end lets its header go up.
		setTuIndiceTestContent {
			Box(modifier = Modifier.height(640.dp)) {
				EvaluationsView(
					lazyListState = rememberLazyListState(),
					weekGroups = weekGroups,
					selectedWeekKey = EvaluationsWeekKey.Academic(8),
					actions = EvaluationsActions(
						onWeekClick = {},
						onEvaluationClick = { _, _, _ -> },
						onEvaluationEdit = {},
						onEvaluationDelete = {}
					)
				)
			}
		}

		waitForIdle()

		val list = onNodeWithTag(EvaluationsUiTags.EvaluationsList).getUnclippedBoundsInRoot()
		val header = onNodeWithTag(EvaluationsUiTags.evaluationsWeekHeader(8)).getUnclippedBoundsInRoot()

		assertTrue(
			abs((header.top - list.top).value) < 1f,
			"the header of the last week at ${header.top} is at the top of the list at ${list.top}"
		)
		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(7))
	}
}
