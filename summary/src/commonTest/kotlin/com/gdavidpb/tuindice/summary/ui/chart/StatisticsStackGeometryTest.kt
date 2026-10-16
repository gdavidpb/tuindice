package com.gdavidpb.tuindice.summary.ui.chart

import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsStackGeometryTest {

	@Test
	fun when_segmentsAreProportional_then_theySplitTheWidthInOrder() {
		val spans = StatisticsStackGeometry.segmentSpans(values = listOf(1f, 3f), widthDp = 200f)

		assertEquals(
			listOf(
				StatisticsStackGeometry.Span(startDp = 0f, widthDp = 50f),
				StatisticsStackGeometry.Span(startDp = 50f, widthDp = 150f)
			),
			spans
		)
	}

	@Test
	fun when_segmentsAddToNothing_then_everySpanHasNoWidth() {
		val spans = StatisticsStackGeometry.segmentSpans(values = listOf(0f, 0f), widthDp = 300f)

		assertEquals(listOf(0f, 0f), spans.map { span -> span.widthDp })
	}

	@Test
	fun when_segmentIsNegative_then_itTakesNoWidthOfItsOwn() {
		val spans = StatisticsStackGeometry.segmentSpans(values = listOf(-2f, 2f), widthDp = 100f)

		assertEquals(
			listOf(
				StatisticsStackGeometry.Span(startDp = 0f, widthDp = 0f),
				StatisticsStackGeometry.Span(startDp = 0f, widthDp = 100f)
			),
			spans
		)
	}

	@Test
	fun when_theBarHasRoomForEachTarget_then_theAreasAreTheSegmentsWithTheirMinimum() {
		val areas = StatisticsStackGeometry.touchSpans(widthsDp = listOf(100f, 100f, 96f), totalDp = 296f)

		assertEquals(listOf(0f, 100f, 200f), areas.map { area -> area.startDp })
		assertEquals(listOf(100f, 100f, 96f), areas.map { area -> area.widthDp })
	}

	@Test
	fun when_narrowSegmentsSitBesideEachOther_then_everyAreaIsAFingerAndTheAreasDoNotOverlap() {
		val areas = StatisticsStackGeometry.touchSpans(widthsDp = listOf(280.9f, 12.8f, 6.4f), totalDp = 300.1f)

		areas.forEach { area -> assertEquals(true, area.widthDp >= 47.9f, "Area is ${area.widthDp}dp") }
		areas.zipWithNext().forEach { (left, right) ->
			assertEquals(left.startDp + left.widthDp, right.startDp, 0.01f)
		}
		assertEquals(300.1f, areas.last().startDp + areas.last().widthDp, 0.01f)
	}

	@Test
	fun when_theBarIsTooNarrowForAFingerEach_then_theAreasShareItAsTheyAre() {
		val areas = StatisticsStackGeometry.touchSpans(widthsDp = listOf(1f, 1f, 1f), totalDp = 3f)

		assertEquals(listOf(1f, 1f, 1f), areas.map { area -> area.widthDp })
	}

	@Test
	fun when_thereIsNoDrawnSegment_then_thereIsNoArea() {
		assertEquals(
			emptyList<StatisticsStackGeometry.Span>(),
			StatisticsStackGeometry.touchSpans(widthsDp = emptyList(), totalDp = 300f)
		)
	}

	@Test
	fun when_aSegmentHasNoValue_then_itIsNotDrawnAndTheDrawnSharesAddUpToOne() {
		val drawn = StatisticsStackGeometry.drawnSegments(listOf(3f, 0f, 1f))

		assertEquals(listOf(0, 2), drawn.map { segment -> segment.index })
		assertEquals(listOf(0.75f, 0.25f), drawn.map { segment -> segment.fraction })
	}

	@Test
	fun when_noSegmentIsDrawn_then_thereAreNoShares() {
		val drawn = StatisticsStackGeometry.drawnSegments(listOf(0f, -2f, Float.NaN))

		assertEquals(emptyList<StatisticsStackGeometry.DrawnSegment>(), drawn)
	}
}
