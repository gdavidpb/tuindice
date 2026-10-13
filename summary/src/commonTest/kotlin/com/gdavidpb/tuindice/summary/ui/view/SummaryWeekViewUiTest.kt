package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
}
