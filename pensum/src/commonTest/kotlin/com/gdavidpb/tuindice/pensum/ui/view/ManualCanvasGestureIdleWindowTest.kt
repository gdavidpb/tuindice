package com.gdavidpb.tuindice.pensum.ui.view

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ManualCanvasGestureIdleWindowTest {
	@Test
	fun when_signalsKeepComing_then_overlaysReturnIdleMillisAfterTheLastSignal() = runTest {
		var isHidden = false
		val window = ManualCanvasGestureIdleWindow(
			scope = this,
			idleMillis = CanvasManualGestureIdleMillis.toLong(),
			nowMillis = { testScheduler.currentTime },
			onOverlaysHiddenChange = { hidden -> isHidden = hidden }
		)

		window.signal()
		assertTrue(isHidden, "The first signal must hide the overlays.")

		testScheduler.advanceTimeBy(200L)
		window.signal()
		testScheduler.advanceTimeBy((CanvasManualGestureIdleMillis - 1).toLong())
		testScheduler.runCurrent()
		assertTrue(isHidden, "The overlays must stay hidden until idleMillis after the last signal.")

		testScheduler.advanceTimeBy(1L)
		testScheduler.runCurrent()
		assertFalse(isHidden, "The overlays must return idleMillis after the last signal.")
	}

	@Test
	fun when_signalArrivesAfterTheWindowEnded_then_overlaysHideAgainWithANewWindow() = runTest {
		var isHidden = false
		val window = ManualCanvasGestureIdleWindow(
			scope = this,
			idleMillis = CanvasManualGestureIdleMillis.toLong(),
			nowMillis = { testScheduler.currentTime },
			onOverlaysHiddenChange = { hidden -> isHidden = hidden }
		)

		window.signal()
		testScheduler.advanceTimeBy(CanvasManualGestureIdleMillis.toLong())
		testScheduler.runCurrent()
		assertFalse(isHidden, "The window must end idleMillis after its only signal.")

		testScheduler.advanceTimeBy(100L)
		window.signal()
		assertTrue(isHidden, "A signal after the window ended must hide the overlays again.")

		testScheduler.advanceTimeBy(CanvasManualGestureIdleMillis.toLong())
		testScheduler.runCurrent()
		assertFalse(isHidden, "The new window must end idleMillis after its own signal.")
	}

	@Test
	fun when_signalsArriveEveryHundredMillis_then_overlaysStayHiddenUntilTheMovementStops() = runTest {
		var isHidden = false
		val window = ManualCanvasGestureIdleWindow(
			scope = this,
			idleMillis = CanvasManualGestureIdleMillis.toLong(),
			nowMillis = { testScheduler.currentTime },
			onOverlaysHiddenChange = { hidden -> isHidden = hidden }
		)

		repeat(10) {
			window.signal()
			testScheduler.advanceTimeBy(100L)
			testScheduler.runCurrent()
			assertTrue(isHidden, "The overlays must stay hidden while the signals keep coming.")
		}

		testScheduler.advanceTimeBy(CanvasManualGestureIdleMillis.toLong())
		testScheduler.runCurrent()
		assertFalse(isHidden, "The overlays must return once the signals stop.")
	}
}
