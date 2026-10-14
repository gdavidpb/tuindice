package com.gdavidpb.tuindice.summary.ui.chart

import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsChartGeometryTest {
	@Test
	fun when_countIsAboveTheDenseThreshold_then_everyColumnIsTheMinimumTarget() {
		assertEquals(48f, StatisticsChartGeometry.columnWidth(availableWidthDp = 1_000f, count = 7))
		assertEquals(60f, StatisticsChartGeometry.columnWidth(availableWidthDp = 360f, count = 6))
	}

	@Test
	fun when_countIsSparse_then_columnsShareTheAvailableWidth() {
		assertEquals(60f, StatisticsChartGeometry.columnWidth(availableWidthDp = 300f, count = 5))
		assertEquals(300f, StatisticsChartGeometry.contentWidth(columnWidthDp = 60f, count = 5))
	}

	@Test
	fun when_sharedColumnWouldBeNarrowerThanATarget_then_itKeepsTheMinimumTarget() {
		assertEquals(48f, StatisticsChartGeometry.columnWidth(availableWidthDp = 200f, count = 5))
	}

	@Test
	fun when_thereAreNoColumns_then_theyTakeNoWidth() {
		assertEquals(0f, StatisticsChartGeometry.columnWidth(availableWidthDp = 300f, count = 0))
		assertEquals(0f, StatisticsChartGeometry.contentWidth(columnWidthDp = 48f, count = 0))
	}

	@Test
	fun when_barExceedsItsScale_then_itFillsTheBandAndNegativeScaleLeavesItEmpty() {
		assertEquals(1f, StatisticsChartGeometry.barFraction(value = 5f, scale = 4f))
		assertEquals(0f, StatisticsChartGeometry.barFraction(value = 3f, scale = 0f))
		assertEquals(0f, StatisticsChartGeometry.barFraction(value = 3f, scale = -1f))
	}

	@Test
	fun when_valueIsOutsideTheRange_then_itIsClampedAndAnEmptyRangePutsItInTheMiddle() {
		assertEquals(1f, StatisticsChartGeometry.valueFraction(value = 9f, minValue = 1f, maxValue = 5f))
		assertEquals(0f, StatisticsChartGeometry.valueFraction(value = -3f, minValue = 1f, maxValue = 5f))
		assertEquals(0.5f, StatisticsChartGeometry.valueFraction(value = 3f, minValue = 1f, maxValue = 5f))
		assertEquals(0.5f, StatisticsChartGeometry.valueFraction(value = 4f, minValue = 4f, maxValue = 4f))
	}

	@Test
	fun when_segmentsAreProportional_then_theySplitTheWidthInOrder() {
		val spans = StatisticsChartGeometry.segmentSpans(values = listOf(1f, 3f), widthDp = 200f)

		assertEquals(
			listOf(
				StatisticsChartGeometry.Span(startDp = 0f, widthDp = 50f),
				StatisticsChartGeometry.Span(startDp = 50f, widthDp = 150f)
			),
			spans
		)
	}

	@Test
	fun when_segmentsAddToNothing_then_everySpanHasNoWidth() {
		val spans = StatisticsChartGeometry.segmentSpans(values = listOf(0f, 0f), widthDp = 300f)

		assertEquals(listOf(0f, 0f), spans.map { span -> span.widthDp })
	}

	@Test
	fun when_segmentIsNegative_then_itTakesNoWidthOfItsOwn() {
		val spans = StatisticsChartGeometry.segmentSpans(values = listOf(-2f, 2f), widthDp = 100f)

		assertEquals(
			listOf(
				StatisticsChartGeometry.Span(startDp = 0f, widthDp = 0f),
				StatisticsChartGeometry.Span(startDp = 0f, widthDp = 100f)
			),
			spans
		)
	}

	@Test
	fun when_valueIsTheMaximum_then_itsYIsTheTopOfTheBand() {
		assertEquals(24f, StatisticsChartGeometry.yOf(fraction = 1f, topDp = 24f, heightDp = 120f))
		assertEquals(144f, StatisticsChartGeometry.yOf(fraction = 0f, topDp = 24f, heightDp = 120f))
	}

	@Test
	fun when_barIsWide_then_itIsCappedAndNarrowOnesScaleWithTheirColumn() {
		assertEquals(32f, StatisticsChartGeometry.barWidth(columnWidthDp = 200f))
		assertEquals(18f, StatisticsChartGeometry.barWidth(columnWidthDp = 30f))
	}

	@Test
	fun when_scaleIsRequested_then_itIsTheLargestValueOrTheReference() {
		assertEquals(13.8f, StatisticsChartGeometry.scaleOf(values = listOf(2f, 5f), reference = 13.8f))
		assertEquals(5f, StatisticsChartGeometry.scaleOf(values = listOf(2f, 5f), reference = null))
		assertEquals(0f, StatisticsChartGeometry.scaleOf(values = emptyList(), reference = null))
	}

	@Test
	fun when_theBarHasRoomForEachTarget_then_theAreasAreTheSegmentsWithTheirMinimum() {
		val areas = StatisticsChartGeometry.touchSpans(widthsDp = listOf(100f, 100f, 96f), totalDp = 296f)

		assertEquals(listOf(0f, 100f, 200f), areas.map { area -> area.startDp })
		assertEquals(listOf(100f, 100f, 96f), areas.map { area -> area.widthDp })
	}

	@Test
	fun when_narrowSegmentsSitBesideEachOther_then_everyAreaIsAFingerAndTheAreasDoNotOverlap() {
		val areas = StatisticsChartGeometry.touchSpans(widthsDp = listOf(280.9f, 12.8f, 6.4f), totalDp = 300.1f)

		areas.forEach { area -> assertEquals(true, area.widthDp >= 47.9f, "Area is ${area.widthDp}dp") }
		areas.zipWithNext().forEach { (left, right) ->
			assertEquals(left.startDp + left.widthDp, right.startDp, 0.01f)
		}
		assertEquals(300.1f, areas.last().startDp + areas.last().widthDp, 0.01f)
	}

	@Test
	fun when_theBarIsTooNarrowForAFingerEach_then_theAreasShareItAsTheyAre() {
		val areas = StatisticsChartGeometry.touchSpans(widthsDp = listOf(1f, 1f, 1f), totalDp = 3f)

		assertEquals(listOf(1f, 1f, 1f), areas.map { area -> area.widthDp })
	}

	@Test
	fun when_thereIsNoDrawnSegment_then_thereIsNoArea() {
		assertEquals(
			emptyList<StatisticsChartGeometry.Span>(),
			StatisticsChartGeometry.touchSpans(widthsDp = emptyList(), totalDp = 300f)
		)
	}
}
