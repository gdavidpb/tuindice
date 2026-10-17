package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.navigation.AssistantLauncher
import com.gdavidpb.tuindice.base.presentation.navigation.LocalAssistantLauncher
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The assistant's action exists only where a launcher is provided, and it asks about the row it was swiped on.
@OptIn(ExperimentalTestApi::class)
class EvaluationSwipeAskUiTest {
	@Test
	fun when_assistantIsAvailable_then_askLeadsTheActions_andTappingItAsksAboutThatRow() = runTuIndiceUiTest {
		val asked = mutableListOf<AssistantScreenContext>()
		val rows = listOf(
			evaluationItemFixture(evaluationId = "evaluation-first"),
			evaluationItemFixture(evaluationId = "evaluation-second")
		)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalAssistantLauncher provides AssistantLauncher { asked += it }) {
				RowsUnderTest(items = rows)
			}
		}

		onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)[1]
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeAskAction).fetchSemanticsNodes().isNotEmpty()
		}

		val ask = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeAskAction).getUnclippedBoundsInRoot()
		val edit = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).getUnclippedBoundsInRoot()

		assertTrue(ask.right <= edit.left, "ask goes before edit")

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeAskAction).performClick()

		// The launcher hears about the row that was swiped, not about the first one.
		assertEquals(listOf(rows[1].assistantContext), asked)
	}

	@Test
	fun when_assistantIsAvailable_then_editAndDeleteKeepTheirCallbacks() = runTuIndiceUiTest {
		val edited = mutableListOf<String>()
		val deleted = mutableListOf<String>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalAssistantLauncher provides AssistantLauncher { _ -> }) {
				RowsUnderTest(
					items = listOf(evaluationItemFixture(evaluationId = "evaluation-first")),
					onEdit = { evaluationId -> edited += evaluationId },
					onDelete = { evaluationId -> deleted += evaluationId }
				)
			}
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).performClick()

		assertEquals(listOf("evaluation-first"), edited)
		assertEquals(emptyList(), deleted)

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).performClick()

		assertEquals(listOf("evaluation-first"), edited)
		assertEquals(listOf("evaluation-first"), deleted)
	}

	@Test
	fun when_noAssistantIsProvided_then_theRowRevealsOnlyEditAndDelete() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RowsUnderTest(items = listOf(evaluationItemFixture(evaluationId = "evaluation-first")))
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		assertNodeHidden(EvaluationsUiTags.EvaluationSwipeAskAction)
		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction)
	}

	@Test
	fun when_askIsGiven_then_theThreeActionsShareTheRevealedWidth_andAskLeads() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(
				modifier = Modifier
					.width(ActionsWithAskWidth)
					.height(CONTAINER_HEIGHT)
			) {
				EvaluationActionsContainer(
					modifier = Modifier.matchParentSize(),
					onAsk = {},
					onEdit = {},
					onDelete = {}
				)
			}
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeAskAction)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeAskAction)
			.assertWidthIsEqualTo(ActionsWithAskWidth / 3)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction)
			.assertWidthIsEqualTo(ActionsWithAskWidth / 3)
		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction)
			.assertWidthIsEqualTo(ActionsWithAskWidth / 3)

		val ask = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeAskAction).getUnclippedBoundsInRoot()
		val edit = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).getUnclippedBoundsInRoot()
		val delete = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).getUnclippedBoundsInRoot()

		assertTrue(ask.right <= edit.left, "ask goes before edit")
		assertTrue(edit.right <= delete.left, "edit goes before delete")
	}

	@Composable
	private fun RowsUnderTest(
		items: List<EvaluationItem>,
		onEdit: (String) -> Unit = {},
		onDelete: (String) -> Unit = {}
	) {
		EvaluationsView(
			lazyListState = rememberLazyListState(),
			weekGroups = evaluationsWeekGroupItemsFixture(groups = listOf(EvaluationsGroupItem(items = items))),
			selectedWeekKey = EvaluationsWeekKey.Academic(8),
			onVisibleWeekChange = {},
			onEvaluationClick = { _, _, _ -> },
			onEvaluationEdit = onEdit,
			onEvaluationDelete = onDelete
		)
	}

	private companion object {
		val CONTAINER_HEIGHT = 72.dp
	}
}
