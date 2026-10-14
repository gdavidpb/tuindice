package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StatisticsBarChartUiTest {
	@Test
	fun when_barIsTapped_then_itsTooltipShowsAndTheOthersDoNot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarChartHost(values = listOf(2f, 4f, 3f), maxValue = 4f, reference = null)
		}

		onNodeWithTag(targetTag(1)).performTouchInput { click() }

		assertTooltipShows("Barra 1")
		assertNoTooltip("Barra 0")
	}

	@Test
	fun when_anotherBarIsTapped_then_theOpenTooltipCloses() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarChartHost(values = listOf(2f, 4f, 3f), maxValue = 4f, reference = null)
		}

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertTooltipShows("Barra 0")

		onNodeWithTag(targetTag(2)).performTouchInput { click() }
		waitUntilAbsentText("Barra 0")
		assertTooltipShows("Barra 2")
	}

	@Test
	fun when_animationsAreDisabled_then_barsStandAtTheirFinalHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				BarChartHost(values = listOf(2f, 4f), maxValue = 4f, reference = null)
			}
		}
		waitForIdle()

		assertEquals(HalfPlotPx, barHeight(0))
		assertEquals(FullPlotPx, barHeight(1))
	}

	@Test
	fun when_animationsAreEnabled_then_barsGrowToTheirFinalHeightOverTheStandardTime() = runTuIndiceUiTest {
		// The clock stands still until the test moves it, so each step of the growth can be read.
		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			BarChartHost(values = listOf(4f), maxValue = 4f, reference = null)
		}
		waitForIdle()
		assertEquals(0, barHeight(0), "Bar starts from nothing")

		mainClock.advanceTimeBy(GrowHalfMillis)
		waitForIdle()
		val halfway = barHeight(0)
		assertTrue(halfway in 1 until FullPlotPx, "Bar is ${halfway}px halfway through its growth")

		mainClock.advanceTimeBy(GrowMillis)
		waitForIdle()
		assertEquals(FullPlotPx, barHeight(0))
	}

	@Test
	fun when_moreThanSixBars_then_itOpensScrolledToTheLatestBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarChartHost(values = List(12) { 1f }, maxValue = 4f, reference = null)
		}
		waitForIdle()

		assertNodeVisible(targetTag(11))
		onNodeWithTag(targetTag(0)).assertIsNotDisplayed()
	}

	@Test
	fun when_referenceIsGiven_then_itsLabelIsShownAtItsEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarChartHost(
				values = listOf(10f, 16f),
				maxValue = 20f,
				reference = StatisticsReference(value = 13.8f, label = "prom. 13.8")
			)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsReferenceLabel)
		onNodeWithTag(SummaryUiTags.StatisticsReferenceLabel).assertTextEquals("prom. 13.8")
	}

	@Test
	fun when_rendered_then_theChartIsDescribedAndEachBarIsAButtonWithItsOwnName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BarChartHost(values = listOf(2f, 4f), maxValue = 4f, reference = null)
		}

		onNodeWithTag(SummaryUiTags.statisticsChart(ChartKey)).assertContentDescriptionEquals(Summary)
		onNodeWithTag(targetTag(0))
			.assertContentDescriptionEquals("Barra 0")
			.assertHasClickAction()
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

		val onClick = onNodeWithTag(targetTag(0)).fetchSemanticsNode().config[SemanticsActions.OnClick]
		assertEquals(ActionLabel, onClick.label)
	}

	@Composable
	private fun BarChartHost(
		values: List<Float>,
		maxValue: Float?,
		reference: StatisticsReference?
	) {
		StatisticsBarChart(
			bars = values.mapIndexed { index, value -> bar(index, value) },
			maxValue = maxValue,
			reference = reference,
			labels = StatisticsChartLabels(
				key = ChartKey,
				summary = Summary,
				actionLabel = ActionLabel
			),
			modifier = Modifier
		)
	}

	private fun bar(index: Int, value: Float): StatisticsBarPoint {
		return StatisticsBarPoint(
			label = "Barra $index",
			value = value,
			tooltip = RichTooltipText(
				title = "Barra $index",
				lines = listOf("Valor $value")
			)
		)
	}

	// The bar sits inside its target, a button that merges what is under it, so only the unmerged tree names the bar.
	private fun ComposeUiTest.barHeight(index: Int): Int {
		return onNodeWithTag(
			testTag = SummaryUiTags.statisticsFill(ChartKey, index),
			useUnmergedTree = true
		).fetchSemanticsNode().size.height
	}

	private fun targetTag(index: Int): String = SummaryUiTags.statisticsTarget(ChartKey, index)

	private companion object {
		const val ChartKey = "bar_test"
		const val Summary = "Barras por valor"
		const val ActionLabel = "Ver detalle"

		// The plot is the chart's height less its reference label space: 168 - 20 dp, at a density of 1.
		const val FullPlotPx = 148
		const val HalfPlotPx = 74
		const val GrowMillis = 300L
		const val GrowHalfMillis = 150L
	}
}
