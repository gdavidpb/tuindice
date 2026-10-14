package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StatisticsLineChartUiTest {
	@Test
	fun when_pointIsTapped_then_itsTooltipShowsAndTheOthersDoNot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LineChartHost(count = 3)
		}

		onNodeWithTag(targetTag(1)).performTouchInput { click() }

		assertTooltipShows("Trimestre 1")
		assertNoTooltip("Trimestre 0")
		assertNoTooltip("Trimestre 2")
	}

	@Test
	fun when_anotherPointIsTapped_then_theOpenTooltipCloses() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LineChartHost(count = 3)
		}

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertTooltipShows("Trimestre 0")

		onNodeWithTag(targetTag(2)).performTouchInput { click() }
		waitUntilAbsentText("Trimestre 0")
		assertTooltipShows("Trimestre 2")
	}

	@Test
	fun when_moreThanSixPoints_then_itOpensScrolledToTheLatestPoint() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LineChartHost(count = 12)
		}
		waitForIdle()

		assertNodeVisible(targetTag(11))
		onNodeWithTag(targetTag(0)).assertIsNotDisplayed()
	}

	@Test
	fun when_rendered_then_theChartIsDescribedAndEachPointIsAButtonWithItsOwnName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LineChartHost(count = 3)
		}

		onNodeWithTag(SummaryUiTags.statisticsChart(ChartKey)).assertContentDescriptionEquals(Summary)
		onNodeWithTag(targetTag(0))
			.assertContentDescriptionEquals("Trimestre 0")
			.assertHasClickAction()
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

		val onClick = onNodeWithTag(targetTag(0)).fetchSemanticsNode().config[SemanticsActions.OnClick]
		assertEquals(ActionLabel, onClick.label)
	}

	@Test
	fun when_pointIsRendered_then_itsTargetIsAtLeastAFingerWide() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LineChartHost(count = 3)
		}

		val size = onNodeWithTag(targetTag(0)).fetchSemanticsNode().size
		assertTrue(size.width >= MinTargetPx && size.height >= MinTargetPx, "Target measures $size")
	}

	@Composable
	private fun LineChartHost(count: Int) {
		StatisticsLineChart(
			points = (0 until count).map { index -> point(index) },
			minValue = 1f,
			maxValue = 5f,
			labels = StatisticsChartLabels(
				key = ChartKey,
				summary = Summary,
				actionLabel = ActionLabel
			),
			modifier = Modifier
		)
	}

	private fun point(index: Int): StatisticsLinePoint {
		return StatisticsLinePoint(
			label = "Trimestre $index",
			lineValue = 2f + index * 0.1f,
			tooltip = RichTooltipText(
				title = "Trimestre $index",
				lines = listOf("Índice ${2 + index}", "Acumulado 3.0")
			)
		)
	}

	private fun targetTag(index: Int): String = SummaryUiTags.statisticsTarget(ChartKey, index)

	private companion object {
		const val ChartKey = "line_test"
		const val Summary = "Índice por trimestre"
		const val ActionLabel = "Ver detalle"
		const val MinTargetPx = 48
	}
}
