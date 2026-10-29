package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The growth of a chart is read where its bars are laid out, so a frame of the growth composes no column and no bar.
// The probe names the parts that compose: after the start of the growth, no frame may compose them again.
@OptIn(ExperimentalTestApi::class)
class StatisticsChartGrowthUiTest {
	@Test
	fun when_barsGrow_then_theColumnsComposeOnlyAtTheStartAndTheBarsStillRise() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides true) {
					BarChartHost(values = listOf(2f, 4f, 3f))
				}
			}
		}

		// The first frame starts the growth: the columns compose for their start, and no later frame composes them.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val settled = compositions.countsByName()
		assertTrue(settled.getValue(BARS_PROBE) >= 1, "the columns compose for their start")

		var midway = 0
		repeat(GROWTH_FRAMES) { frame ->
			mainClock.advanceTimeByFrame()
			waitForIdle()
			assertEquals(settled, compositions.countsByName(), "frame ${frame + 2} composes no column")

			if (frame == MIDWAY_FRAME) midway = barHeight(index = 1)
		}

		assertTrue(midway in 1 until FULL_PLOT_PX, "the bar is ${midway}px on its way up while no column composes")
		assertEquals(FULL_PLOT_PX, barHeight(index = 1))
	}

	@Test
	fun when_stackedBarGrows_then_itComposesOnlyAtTheStartAndItsSegmentsStillGrow() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides true) {
					StackedBarHost(values = listOf(1f, 3f))
				}
			}
		}

		// The first frame starts the growth: the bar composes for its start, and no later frame composes it.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val settled = compositions.countsByName()
		assertTrue(settled.getValue(STACKED_PROBE) >= 1, "the bar composes for its start")

		var midway = 0
		repeat(GROWTH_FRAMES) { frame ->
			mainClock.advanceTimeByFrame()
			waitForIdle()
			assertEquals(settled, compositions.countsByName(), "frame ${frame + 2} composes no bar")

			if (frame == MIDWAY_FRAME) midway = segmentWidth(index = 1)
		}

		assertTrue(midway in 1 until THREE_QUARTERS_PX, "the segment is ${midway}px on its way across")
		assertEquals(THREE_QUARTERS_PX, segmentWidth(index = 1))
	}

	@Test
	fun when_theBarsHaveGrown_then_theyAndTheirFiguresStandWhereTheyStandWithoutAnimations() = runTuIndiceUiTest {
		var animations by mutableStateOf(true)
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides animations) {
				BarChartHost(values = listOf(2f, 4f, 3f), valueLabel = { index -> "Cifra $index" })
			}
		}
		waitForIdle()
		repeat(GROWTH_FRAMES) { mainClock.advanceTimeByFrame() }
		waitForIdle()

		val grown = boundsOf(barTags())
		assertEquals(FULL_PLOT_PX, barHeight(index = 1))

		// The same chart with its animations off, once the growth is over: every bar and figure stands where it stood.
		animations = false
		waitForIdle()

		assertEquals(grown, boundsOf(barTags()))
	}

	@Test
	fun when_theStackedBarHasGrown_then_itsSegmentsStandWhereTheyStandWithoutAnimations() = runTuIndiceUiTest {
		var animations by mutableStateOf(true)
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides animations) {
				StackedBarHost(values = listOf(1f, 3f))
			}
		}
		waitForIdle()
		repeat(GROWTH_FRAMES) { mainClock.advanceTimeByFrame() }
		waitForIdle()

		val grown = boundsOf(segmentTags())
		assertEquals(THREE_QUARTERS_PX, segmentWidth(index = 1))

		// The same bar with its animations off, once the growth is over: each segment and target stands where it stood.
		animations = false
		waitForIdle()

		assertEquals(grown, boundsOf(segmentTags()))
	}

	@Composable
	private fun BarChartHost(
		values: List<Float>,
		valueLabel: (Int) -> String? = { null }
	) {
		StatisticsBarChart(
			bars = values.mapIndexed { index, value -> bar(index, value, valueLabel(index)) },
			maxValue = FULL_SCALE,
			reference = null,
			labels = StatisticsChartLabels(
				key = BARS_KEY,
				summary = BARS_SUMMARY,
				actionLabel = ACTION_LABEL
			),
			modifier = Modifier
		)
	}

	@Composable
	private fun StackedBarHost(values: List<Float>) {
		StatisticsStackedBar(
			segments = values.mapIndexed { index, value -> segment(index, value) },
			labels = StatisticsChartLabels(
				key = STACKED_KEY,
				summary = STACKED_SUMMARY,
				actionLabel = ACTION_LABEL
			),
			modifier = Modifier
		)
	}

	private fun bar(index: Int, value: Float, valueLabel: String?): StatisticsBarPoint {
		return StatisticsBarPoint(
			label = "Barra $index",
			value = value,
			tooltip = RichTooltipText(
				title = "Barra $index",
				lines = listOf("Valor $value")
			),
			axisLabel = "Eje $index",
			valueLabel = valueLabel
		)
	}

	private fun segment(index: Int, value: Float): StatisticsSegment {
		return StatisticsSegment(
			label = "Segmento $index",
			value = value,
			color = Color(0xFF2E7D32),
			tooltip = RichTooltipText(
				title = "Segmento $index",
				lines = listOf("Valor $value")
			)
		)
	}

	// The bar sits inside its target, a button that merges what is under it, so only the unmerged tree names the bar.
	private fun ComposeUiTest.barHeight(index: Int): Int {
		return onNodeWithTag(
			testTag = SummaryUiTags.statisticsFill(BARS_KEY, index),
			useUnmergedTree = true
		).fetchSemanticsNode().size.height
	}

	private fun ComposeUiTest.segmentWidth(index: Int): Int {
		return onNodeWithTag(
			testTag = SummaryUiTags.statisticsFill(STACKED_KEY, index),
			useUnmergedTree = true
		).fetchSemanticsNode().size.width
	}

	// Where each node stands in the window, in pixels: the unmerged tree keeps the figures under their targets.
	private fun ComposeUiTest.boundsOf(tags: List<String>): Map<String, Rect> {
		return tags.associateWith { tag ->
			onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		}
	}

	private fun barTags(): List<String> {
		return (0..2).flatMap { index ->
			listOf(
				SummaryUiTags.statisticsFill(BARS_KEY, index),
				StatisticsLabelUiTags.value(BARS_KEY, index),
				SummaryUiTags.statisticsTarget(BARS_KEY, index)
			)
		}
	}

	private fun segmentTags(): List<String> {
		return (0..1).flatMap { index ->
			listOf(
				SummaryUiTags.statisticsFill(STACKED_KEY, index),
				SummaryUiTags.statisticsTarget(STACKED_KEY, index)
			)
		}
	}

	private fun List<String>.countsByName() = groupingBy { it }.eachCount()

	private companion object {
		const val BARS_KEY = "growth_bars"
		const val STACKED_KEY = "growth_stacked"
		const val BARS_SUMMARY = "Barras por valor"
		const val STACKED_SUMMARY = "Materias por resultado"
		const val ACTION_LABEL = "Ver detalle"

		const val BARS_PROBE = "statistics-bars"
		const val STACKED_PROBE = "statistics-stacked"

		const val FULL_SCALE = 4f

		// The plot is the chart's height less its reference label space: 168 - 20 dp, at a density of 1.
		const val FULL_PLOT_PX = 148

		// The stacked bar spans the 360 dp of the compact test scene: its second segment is three quarters of it.
		const val THREE_QUARTERS_PX = 270

		// The growth lasts 300 ms: at 16 ms a frame, 30 frames are past its end, and the fifth frame is on its way up.
		const val GROWTH_FRAMES = 30
		const val MIDWAY_FRAME = 5
	}
}
