package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryWeekViewUiTest {
	private val week = summaryHubItem().week!!

	@Test
	fun when_weekHasAClassAndEvaluations_then_theClassIsFirstAndEveryRowIsThere() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(week = week, actions = SummaryHubActions())
		}

		assertNodeVisible(SummaryUiTags.WeekSection)
		onNodeWithText("Esta semana").assertIsDisplayed()
		onNodeWithText("Horario").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.WeekClassRow)
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-1"))
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-2"))
	}

	@Test
	fun when_weekHasNoClass_then_onlyTheEvaluationsAreListed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(week = week.copy(classRow = null), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.WeekClassRow).assertDoesNotExist()
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-1"))
	}

	@Test
	fun when_theTermHasNoSchedule_then_theWeekOffersNoLinkToIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(week = week.copy(classRow = null, hasSchedule = false), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).assertDoesNotExist()
		onNodeWithText("Horario").assertDoesNotExist()
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-1"))
	}

	@Test
	fun when_rowsAndTheLinkAreTapped_then_eachAsksForItsScreen() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryWeekView(
				week = week,
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).performClick()
		onNodeWithTag(SummaryUiTags.WeekClassRow).performClick()
		onNodeWithTag(SummaryUiTags.weekEvaluationRow("evaluation-1")).performClick()

		assertEquals(
			listOf(
				SummaryHubTarget.Schedule,
				SummaryHubTarget.Schedule,
				SummaryHubTarget.EvaluationsWeek(weekStart = LocalDate(2026, 10, 12))
			),
			targets
		)
	}

	@Test
	fun when_theClassIsInProgressOnANarrowScreen_then_itsContentStartsWhereTheOnesOfTheEvaluationsDo() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryWeekView(week = week, actions = SummaryHubActions())
				}
			}

			// The blocks and the room are said by the class row and by the first evaluation row, each at the
			// start of its content.
			val places = onAllNodesWithText("3-4 · MYS-210", useUnmergedTree = true).fetchSemanticsNodes()
			val starts = places.map { place -> place.boundsInRoot.left }

			assertEquals(2, starts.size)
			assertTrue(
				kotlin.math.abs(starts[0] - starts[1]) < 0.5f,
				"the class row starts its content at ${starts[0]} px and the evaluation row at ${starts[1]} px"
			)
		}
}
