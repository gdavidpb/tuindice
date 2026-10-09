package com.gdavidpb.tuindice.scenariorunner.driver

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent

/** Injects the events of a swipe ([SwipeTrack]) with the times they carry, and writes how long the swipe took. */
internal class SwipeInjector(private val session: DeviceSession) {
	/**
	 * Injects the events of [track] and returns when the nominal duration of the gesture has passed; true when the whole
	 * gesture reached the system. The events carry their own times, so they are injected without waiting for the app to
	 * consume each one; only the last, the up, is waited for, which answers whether the gesture was delivered. The pause
	 * that follows is the part of the nominal duration that the injection did not take, so a swipe costs what it lasts
	 * and not what the host takes. The line it writes to the driver log (`swipe: N events in X ms (nominal Y ms)`) is the
	 * measure of that cost.
	 */
	fun inject(track: List<SwipeTrack.Sample>): Boolean {
		val automation = session.instrumentation.uiAutomation
		val downTime = track.first().eventTime
		var delivered = true

		track.forEachIndexed { index, sample ->
			val event = motionEvent(downTime, sample)

			delivered = automation.injectInputEvent(event, index == track.lastIndex) && delivered
			event.recycle()
		}
		SystemClock.sleep((track.last().eventTime - SystemClock.uptimeMillis()).coerceAtLeast(0))
		session.log.write(
			"swipe: ${track.size} events in ${SystemClock.uptimeMillis() - downTime} ms " +
				"(nominal ${track.last().eventTime - downTime} ms)"
		)

		return delivered
	}

	private fun motionEvent(downTime: Long, sample: SwipeTrack.Sample): MotionEvent {
		val action = when (sample.kind) {
			SwipeTrack.Kind.DOWN -> MotionEvent.ACTION_DOWN
			SwipeTrack.Kind.MOVE -> MotionEvent.ACTION_MOVE
			SwipeTrack.Kind.UP -> MotionEvent.ACTION_UP
		}
		val properties = MotionEvent.PointerProperties().apply {
			id = 0
			toolType = MotionEvent.TOOL_TYPE_FINGER
		}
		val coordinates = MotionEvent.PointerCoords().apply {
			x = sample.x
			y = sample.y
			pressure = 1f
			size = 1f
		}

		return MotionEvent.obtain(
			downTime, sample.eventTime, action, 1, arrayOf(properties), arrayOf(coordinates),
			0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
		)
	}
}
