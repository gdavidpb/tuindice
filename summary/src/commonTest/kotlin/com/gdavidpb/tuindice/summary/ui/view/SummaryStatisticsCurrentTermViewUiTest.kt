package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsCurrentTermViewUiTest {
	@Test
	fun when_theTermHasRows_then_itsFigureRowsAndOverdueLineAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CurrentTermHost(content = termData(overdue = overdueOf(target = null)), termLink = "Sep - Dic 2026")
		}

		onNodeWithText("10").assertIsDisplayed()
		onNodeWithText("3 %").assertIsDisplayed()
		onNodeWithText("Parciales").assertIsDisplayed()
		onNodeWithText("8 de 10 · 80 %").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsOverdue).performScrollTo().assertIsDisplayed()
		onNodeWithText("vencidas sin nota").performScrollTo().assertIsDisplayed()
	}

	@Test
	fun when_aRowWithAGradeIsTapped_then_itsTooltipShowsItsPointsAndShare() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CurrentTermHost(content = termData(overdue = null), termLink = null)
		}

		onNodeWithTag(SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.CurrentTerm, 1))
			.performScrollTo()
			.performClick()

		assertTooltipShows("Quices: 8 de 10 (80 %)")
	}

	@Test
	fun when_theTermLinkIsTapped_then_itAsksForRecord() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			CurrentTermHost(
				content = termData(overdue = null),
				termLink = "Sep - Dic 2026",
				onTarget = { target -> targets += target }
			)
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.StatisticsCurrentTermBlock)).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Record), targets)
	}

	@Test
	fun when_theEvaluationsAreNotDownloaded_then_theActionAsksForEvaluations() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			CurrentTermHost(
				content = SummaryStatisticsCurrentTermItem.Content.NotSynced(
					title = words("Tus evaluaciones no están aquí"),
					message = words("Ábrelas en Evaluaciones para verlas aquí."),
					action = words("Ir a Evaluaciones"),
					target = SummaryHubTarget.Evaluations
				),
				onTarget = { target -> targets += target }
			)
		}

		onNodeWithText("Ir a Evaluaciones").performScrollTo().performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Evaluations), targets)
	}

	@Test
	fun when_thereIsNoTerm_then_theInvitationIsWrittenAndTheHeadHasNoLink() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CurrentTermHost(
				content = SummaryStatisticsCurrentTermItem.Content.NoTerm(
					title = words("Sin trimestre en curso"),
					message = words("Cuando inscribas, aquí verás cómo van tus evaluaciones.")
				),
				termLink = null,
				termTarget = null
			)
		}

		onNodeWithText("Sin trimestre en curso").assertIsDisplayed()
		onAllNodesWithTag(SummaryUiTags.sectionLink(SummaryUiTags.StatisticsCurrentTermBlock)).assertCountEquals(0)
	}

	@Test
	fun when_theOverdueLineIsTapped_then_itAsksForTheWeekOfTheOldestOne() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()
		val week = SummaryHubTarget.EvaluationsWeek(weekStart = LocalDate(2026, 10, 5), evaluationId = "evaluation-1")

		setTuIndiceTestContent {
			CurrentTermHost(
				content = termData(overdue = overdueOf(target = week)),
				onTarget = { target -> targets += target }
			)
		}

		onNodeWithTag(SummaryUiTags.StatisticsOverdue).performScrollTo().performClick()

		assertEquals(listOf<SummaryHubTarget>(week), targets)
	}

	// The block sits in a scrolling column, as it does on the screen, so its last line can be reached.
	@Composable
	private fun CurrentTermHost(
		content: SummaryStatisticsCurrentTermItem.Content,
		termLink: String? = "Sep - Dic 2026",
		termTarget: SummaryHubTarget? = SummaryHubTarget.Record,
		onTarget: (SummaryHubTarget) -> Unit = {}
	) {
		Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
			SummaryStatisticsCurrentTermView(
				item = SummaryStatisticsCurrentTermItem(
					title = words("Este trimestre"),
					termLink = termLink?.let(::words),
					termTarget = termTarget,
					content = content
				),
				chartActionLabel = "Ver detalle",
				actions = SummaryHubActions(onTargetClick = onTarget)
			)
		}
	}

	private fun termData(overdue: SummaryStatisticsCurrentTermItem.Overdue?): SummaryStatisticsCurrentTermItem.Content {
		return SummaryStatisticsCurrentTermItem.Content.Data(
			figure = figureOf(value = "10", label = "de 400 puntos evaluados", trailing = "3 %"),
			fraction = 0.025f,
			rows = listOf(
				SummaryStatisticsEvaluationTypeItem(
					name = words("Parciales"),
					gradedText = words("0 de 4 calificadas"),
					scoreText = words("Sin notas aún"),
					fraction = 0f,
					tooltip = null
				),
				SummaryStatisticsEvaluationTypeItem(
					name = words("Quices"),
					gradedText = words("1 de 1 calificada"),
					scoreText = words("8 de 10 · 80 %"),
					fraction = 0.8f,
					tooltip = tooltipOf("Quices: 8 de 10 (80 %)", "1 de 1 calificada")
				)
			),
			overdue = overdue
		)
	}

	private fun overdueOf(target: SummaryHubTarget.EvaluationsWeek?): SummaryStatisticsCurrentTermItem.Overdue {
		return SummaryStatisticsCurrentTermItem.Overdue(
			fact = SummaryStatisticsFactItem(value = "2", label = words("vencidas sin nota")),
			target = target
		)
	}
}
