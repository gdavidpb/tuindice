package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationsActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

// The probes report the row's swipe state and its content on each composition. A gesture must not compose either of
// them on each of its frames: each composes when the row is shown, and at most twice more while it opens or closes.
@OptIn(ExperimentalTestApi::class)
class EvaluationSwipeRecompositionUiTest {
	@Test
	fun when_rowIsSwipedOpen_then_theRowIsNotRecomposedOnEachFrame() = runTuIndiceUiTest {
		val reports = mutableListOf<String>()

		setTuIndiceTestContent {
			RowsUnderTest(reports = reports)
		}
		waitForIdle()

		val swipeBefore = reports.countOf(swipeReport)
		val rowBefore = reports.countOf(rowReport)

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		// The actions appear as the opening tween crosses the threshold, and the tween runs on after that.
		mainClock.advanceTimeBy(200)
		waitForIdle()

		val swipeDuringOpen = reports.countOf(swipeReport) - swipeBefore
		val rowDuringOpen = reports.countOf(rowReport) - rowBefore

		assertTrue(swipeDuringOpen <= maxReportsPerGesture, "Swipe state recomposed $swipeDuringOpen times")
		assertTrue(rowDuringOpen <= maxReportsPerGesture, "Row recomposed $rowDuringOpen times")
	}

	@Test
	fun when_rowIsSwipedBack_then_theRowIsNotRecomposedOnEachFrame() = runTuIndiceUiTest {
		val reports = mutableListOf<String>()

		setTuIndiceTestContent {
			RowsUnderTest(reports = reports)
		}
		waitForIdle()

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		val swipeBefore = reports.countOf(swipeReport)
		val rowBefore = reports.countOf(rowReport)

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeRight() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isEmpty()
		}

		// The actions go as the closing tween crosses the threshold, and the tween runs on after that.
		mainClock.advanceTimeBy(200)
		waitForIdle()

		val swipeDuringClose = reports.countOf(swipeReport) - swipeBefore
		val rowDuringClose = reports.countOf(rowReport) - rowBefore

		assertTrue(swipeDuringClose <= maxReportsPerGesture, "Swipe state recomposed $swipeDuringClose times")
		assertTrue(rowDuringClose <= maxReportsPerGesture, "Row recomposed $rowDuringClose times")
	}

	@Composable
	private fun RowsUnderTest(reports: MutableList<String>) {
		CompositionLocalProvider(LocalTuIndiceCompositionProbe provides { name -> reports += name }) {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = evaluationsWeekGroupItemsFixture(
					groups = listOf(
						EvaluationsGroupItem(
							items = listOf(evaluationItemFixture(evaluationId = evaluationId))
						)
					)
				),
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				actions = EvaluationsActions(
					onEvaluationEdit = {},
					onEvaluationDelete = {},
					onAskAssistant = { _ -> }
				)
			)
		}
	}

	private fun List<String>.countOf(report: String): Int = count { reported -> reported == report }

	private companion object {
		const val evaluationId = "evaluation-first"
		const val swipeReport = "evaluation-swipe"
		const val rowReport = "evaluation-row:evaluation-first"
		const val maxReportsPerGesture = 2
	}
}
