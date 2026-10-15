package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsStackedBarUiTest {
	@Test
	fun when_segmentIsTapped_then_itsTooltipShowsAndTheOthersDoNot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(3f, 1f, 1f))
		}

		onNodeWithTag(targetTag(1)).performTouchInput { click() }

		assertTooltipShows("Segmento 1")
		assertNoTooltip("Segmento 0")
	}

	@Test
	fun when_anotherSegmentIsTapped_then_theOpenTooltipCloses() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(3f, 1f, 1f))
		}

		onNodeWithTag(targetTag(1)).performTouchInput { click() }
		assertTooltipShows("Segmento 1")

		onNodeWithTag(targetTag(2)).performTouchInput { click() }
		waitUntilAbsentText("Segmento 1")
		assertTooltipShows("Segmento 2")
	}

	@Test
	fun when_segmentHasNoValue_then_itIsNotDrawnAndHasNoTarget() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(1f, 0f, 2f))
		}
		waitForIdle()

		assertNodeHidden(SummaryUiTags.statisticsFill(ChartKey, 1))
		assertNodeHidden(targetTag(1))
		assertNodeVisible(targetTag(2))
	}

	@Test
	fun when_narrowSegmentSitsBesideAWideOne_then_tappingItsTargetOpensItsOwnTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(1f, 98f, 1f))
		}

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertTooltipShows("Segmento 0")
		assertNoTooltip("Segmento 1")

		onNodeWithTag(targetTag(2)).performTouchInput { click() }
		assertTooltipShows("Segmento 2")
		assertNoTooltip("Segmento 1")
	}

	@Test
	fun when_narrowSegmentIsAtAnEnd_then_itsTargetStaysInsideTheBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(1f, 98f, 1f))
		}
		waitForIdle()

		val bar = onNodeWithTag(SummaryUiTags.statisticsChart(ChartKey)).fetchSemanticsNode().boundsInRoot
		val first = onNodeWithTag(targetTag(0)).fetchSemanticsNode().boundsInRoot
		val last = onNodeWithTag(targetTag(2)).fetchSemanticsNode().boundsInRoot

		assertEquals(bar.left, first.left)
		assertEquals(bar.right, last.right)
	}

	@Test
	fun when_narrowSegmentsSitBesideEachOther_then_bothCanBeTapped() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(44f, 2f, 1f))
		}
		waitForIdle()

		onNodeWithTag(targetTag(1)).performTouchInput { click() }
		assertTooltipShows("Segmento 1")
		assertNoTooltip("Segmento 2")

		onNodeWithTag(targetTag(2)).performTouchInput { click() }
		assertTooltipShows("Segmento 2")
		assertNoTooltip("Segmento 1")
	}

	@Test
	fun when_animationsAreDisabled_then_segmentsStandAtTheirFinalWidths() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				StackedBarHost(values = listOf(1f, 3f))
			}
		}
		waitForIdle()

		assertEquals(QuarterPx, segmentWidth(0))
		assertEquals(ThreeQuartersPx, segmentWidth(1))
	}

	@Test
	fun when_rendered_then_theBarIsDescribedAndEachSegmentIsAButtonWithItsOwnName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(1f, 3f))
		}

		onNodeWithTag(SummaryUiTags.statisticsChart(ChartKey)).assertContentDescriptionEquals(Summary)
		onNodeWithTag(targetTag(0))
			.assertContentDescriptionEquals("Segmento 0")
			.assertHasClickAction()
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

		val onClick = onNodeWithTag(targetTag(0)).fetchSemanticsNode().config[SemanticsActions.OnClick]
		assertEquals(ActionLabel, onClick.label)
	}

	@Test
	fun when_rendered_then_theBarIsTheSharedHeightAndItsTargetsStayAFingerTall() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StackedBarHost(values = listOf(1f, 3f))
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.statisticsFill(ChartKey, 0), useUnmergedTree = true)
			.assertHeightIsEqualTo(TuIndiceComponentSizes.ProgressBarHeight)
		onNodeWithTag(targetTag(0)).assertHeightIsEqualTo(StatisticsChartDefaults.TargetMinSize)
		onNodeWithTag(targetTag(1)).assertHeightIsEqualTo(StatisticsChartDefaults.TargetMinSize)
	}

	@Composable
	private fun StackedBarHost(values: List<Float>) {
		StatisticsStackedBar(
			segments = values.mapIndexed { index, value -> segment(index, value) },
			labels = StatisticsChartLabels(
				key = ChartKey,
				summary = Summary,
				actionLabel = ActionLabel
			),
			modifier = Modifier
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

	// The segment is drawn inside its target, a button that merges what is under it, so only the unmerged tree names it.
	private fun ComposeUiTest.segmentWidth(index: Int): Int {
		return onNodeWithTag(
			testTag = SummaryUiTags.statisticsFill(ChartKey, index),
			useUnmergedTree = true
		).fetchSemanticsNode().size.width
	}

	private fun targetTag(index: Int): String = SummaryUiTags.statisticsTarget(ChartKey, index)

	private companion object {
		const val ChartKey = "stacked_test"
		const val Summary = "Materias por resultado"
		const val ActionLabel = "Ver detalle"

		// The bar spans the 360 dp of the compact test scene: a quarter and three quarters of it.
		const val QuarterPx = 90
		const val ThreeQuartersPx = 270
	}
}
