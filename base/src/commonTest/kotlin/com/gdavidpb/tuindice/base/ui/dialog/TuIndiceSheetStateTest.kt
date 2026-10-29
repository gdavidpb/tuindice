package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TuIndiceSheetStateTest {
	@Test
	fun expandedAnchorPx_whenTheSheetFitsTheWindow_restsItsBottomOnTheWindowBottom() {
		assertEquals(CONTAINER_PX - SHORT_SHEET_PX, expandedAnchorPx(CONTAINER_PX, SHORT_SHEET_PX))
	}

	@Test
	fun limitedSheetHeightPx_whenTheSheetIsTallerThanTheWindowBelowTheStatusBar_isCappedAtThatSpace() {
		assertEquals(
			CONTAINER_PX - TOP_INSET_PX,
			limitedSheetHeightPx(CONTAINER_PX, TALL_SHEET_PX, TOP_INSET_PX)
		)
	}

	@Test
	fun updateAnchors_whenTheSheetIsTallerThanTheWindowBelowTheStatusBar_itsExpandedAnchorStaysBelowTheStatusBar() {
		val sheet = sheet()

		sheet.updateAnchors(CONTAINER_PX, TALL_SHEET_PX, TOP_INSET_PX)

		assertEquals(TOP_INSET_PX, sheet.draggableState.anchors.positionOf(TuIndiceSheetValue.Expanded))
	}

	@Test
	fun updateAnchors_whenTheSheetFitsBelowTheStatusBar_itsBottomEdgeRestsOnTheWindowBottom() {
		val sheet = sheet()

		sheet.updateAnchors(CONTAINER_PX, SHORT_SHEET_PX, TOP_INSET_PX)

		val expandedOffsetPx = sheet.draggableState.anchors.positionOf(TuIndiceSheetValue.Expanded)
		assertEquals(CONTAINER_PX - SHORT_SHEET_PX, expandedOffsetPx)
		assertEquals(CONTAINER_PX, expandedOffsetPx + SHORT_SHEET_PX)
	}

	@Test
	fun updateAnchors_whenTheSheetIsHalfOpenable_thePartiallyExpandedAnchorStaysAtHalfTheWindow() {
		val sheet = sheet()

		sheet.updateAnchors(CONTAINER_PX, HALF_OR_MORE_SHEET_PX, TOP_INSET_PX)

		assertTrue(sheet.hasPartiallyExpandedState)
		assertEquals(
			CONTAINER_PX / 2f,
			sheet.draggableState.anchors.positionOf(TuIndiceSheetValue.PartiallyExpanded)
		)
	}

	@Test
	fun updateAnchors_whenATallSheetIsHalfOpenable_thePartiallyExpandedAnchorStaysAtHalfTheWindow() {
		val sheet = sheet()

		sheet.updateAnchors(CONTAINER_PX, TALL_SHEET_PX, TOP_INSET_PX)

		assertTrue(sheet.hasPartiallyExpandedState)
		assertEquals(
			CONTAINER_PX / 2f,
			sheet.draggableState.anchors.positionOf(TuIndiceSheetValue.PartiallyExpanded)
		)
	}

	@Test
	fun expand_whenItIsAskedBeforeTheFirstLayout_theOpeningRestsExpanded() = runTest {
		val sheet = sheet()

		withContext(TestFrameClock()) {
			sheet.expand()
			sheet.updateAnchors(CONTAINER_PX, HALF_OR_MORE_SHEET_PX, TOP_INSET_PX)
			sheet.show()
		}

		assertEquals(TuIndiceSheetValue.Expanded, sheet.currentValue)
	}

	@Test
	fun hide_whenItIsAskedWhileTheSheetOpens_endsTheOpeningAndRestsHidden() = runTest {
		val sheet = sheet()
		sheet.updateAnchors(CONTAINER_PX, HALF_OR_MORE_SHEET_PX, TOP_INSET_PX)

		withContext(TestFrameClock()) {
			val opening = launch { sheet.show() }
			delay(OPENING_MILLIS)

			sheet.hide()
			opening.join()
		}

		assertEquals(TuIndiceSheetValue.Hidden, sheet.currentValue)
		assertFalse(sheet.consumeUserDismissal())
	}

	@Test
	fun show_whenAReleaseCutsTheOpeningOfANonHideableSheet_theHiddenAnchorIsDropped() = runTest {
		val sheet = sheet(hideable = false)
		sheet.updateAnchors(CONTAINER_PX, HALF_OR_MORE_SHEET_PX, TOP_INSET_PX)

		withContext(TestFrameClock()) {
			val opening = launch { sheet.show() }
			delay(OPENING_MILLIS)

			sheet.settle(velocity = 0f)
			opening.join()
		}

		assertFalse(sheet.draggableState.anchors.hasPositionFor(TuIndiceSheetValue.Hidden))
	}

	private fun sheet(
		skipPartiallyExpanded: Boolean = false,
		hideable: Boolean = true
	): TuIndiceSheetState = TuIndiceSheetState(
		skipPartiallyExpanded = skipPartiallyExpanded,
		hideable = hideable,
		initialValue = TuIndiceSheetValue.Hidden,
		velocityThresholdPx = VELOCITY_THRESHOLD_PX
	)

	// The frames the sheet's animations wait for: one every FRAME_MILLIS of the test's own time, so a test decides how far
	// they run.
	private class TestFrameClock : MonotonicFrameClock {
		private var frameNanos = 0L

		override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
			delay(FRAME_MILLIS)
			frameNanos += FRAME_MILLIS * NANOS_PER_MILLI

			return onFrame(frameNanos)
		}
	}

	private companion object {
		const val CONTAINER_PX = 1_000f
		const val SHORT_SHEET_PX = 400f
		const val HALF_OR_MORE_SHEET_PX = 800f
		const val TALL_SHEET_PX = 1_200f
		const val TOP_INSET_PX = 60f
		const val VELOCITY_THRESHOLD_PX = 125f
		const val FRAME_MILLIS = 16L
		const val OPENING_MILLIS = 2 * FRAME_MILLIS
		const val NANOS_PER_MILLI = 1_000_000L
	}
}
