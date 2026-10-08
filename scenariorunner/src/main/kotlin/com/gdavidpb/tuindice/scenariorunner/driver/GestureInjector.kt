package com.gdavidpb.tuindice.scenariorunner.driver

import android.os.SystemClock
import android.view.KeyEvent
import com.gdavidpb.tuindice.scenariokit.driver.Gestures
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.model.Query

/** Touch input on computed pixels; fractions are of the target's (or the screen's) visible size. */
internal class GestureInjector(private val session: DeviceSession) : Gestures {
	override fun tap(q: Query): Boolean {
		session.log.clearRefusal()
		val settled = session.settledBounds(q)
		val place = settled.bounds ?: return refuse("tap", settled.why)

		return click(place.centerX(), place.centerY(), "tap", "on $q")
	}

	override fun tapAt(q: Query?, fx: Double, fy: Double): Boolean {
		session.log.clearRefusal()
		val aim = area(q)
		val box = aim.box ?: return refuse("tapAt", aim.why)

		return click(box.pointX(fx), box.pointY(fy), "tapAt", q?.toString() ?: "the screen")
	}

	override fun doubleTap(q: Query): Boolean {
		session.log.clearRefusal()
		val aim = area(q)
		val box = aim.box ?: return refuse("doubleTap", aim.why)
		val x = box.pointX(CENTER)
		val y = box.pointY(CENTER)

		return !session.keyboard.covers(x, y, "doubleTap on $q") && runCatching {
			val first = session.device.click(x, y)
			SystemClock.sleep(DOUBLE_TAP_GAP_MS)
			session.device.click(x, y) && first
		}.getOrDefault(false).also { if (!it) refuse("doubleTap", "on $q: the clicks were not delivered") }
	}

	override fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean {
		session.log.clearRefusal()
		val aim = area(from)
		val box = aim.box ?: return refuse("swipe", aim.why)
		val screenWidth = session.device.displayWidth
		val screenHeight = session.device.displayHeight
		val startX = box.pointX(vector.fx)
		val startY = box.pointY(vector.fy)
		val endX = (startX + vector.dx * screenWidth).toInt().coerceIn(EDGE_MARGIN, screenWidth - EDGE_MARGIN)
		val endY = (startY + vector.dy * screenHeight).toInt().coerceIn(EDGE_MARGIN, screenHeight - EDGE_MARGIN)
		val steps = (durationMs / STEP_MS).toInt().coerceAtLeast(1)

		return !session.keyboard.covers(startX, startY, "swipe from ${from ?: "the screen"}") &&
			runCatching { session.device.swipe(startX, startY, endX, endY, steps) }.getOrDefault(false)
				.also { if (!it) refuse("swipe", "from ${from ?: "the screen"}: the swipe was not delivered") }
	}

	/**
	 * Injects the key without waiting for the app to consume it: `UiDevice.pressBack` injects
	 * synchronously and answers false when a busy main thread takes over a second to acknowledge it,
	 * although the key was delivered. The next step waits for the effect.
	 */
	override fun pressBack(): Boolean = runCatching {
		session.log.clearRefusal()
		val automation = session.instrumentation.uiAutomation
		val now = SystemClock.uptimeMillis()
		val down = KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0)
		val up = KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0)

		automation.injectInputEvent(down, false) && automation.injectInputEvent(up, false)
	}.getOrDefault(false).also { if (!it) refuse("pressBack", "the back key could not be injected") }

	/** A click at ([x], [y]) unless that point is inside the on-screen keyboard, where it would press a key. */
	private fun click(x: Int, y: Int, primitive: String, target: String): Boolean {
		if (session.keyboard.covers(x, y, "$primitive $target")) return false

		return runCatching { session.device.click(x, y) }.getOrDefault(false)
			.also { if (!it) refuse(primitive, "$target at ($x, $y): the click was not delivered") }
	}

	/** Writes the refusal of [primitive] to the driver log and answers false, the answer of a refused gesture. */
	private fun refuse(primitive: String, reason: String): Boolean {
		session.log.refuse(primitive, reason)
		return false
	}

	/** Visible rectangle of [q], or the whole screen when [q] is null; no box, and why, when [q] is not on screen. */
	private fun area(q: Query?): Aim {
		if (q == null) return Aim(Box(0, 0, session.device.displayWidth, session.device.displayHeight), "")

		val settled = session.settledBounds(q)

		return Aim(settled.bounds?.let { Box(it.left, it.top, it.right, it.bottom) }, settled.why)
	}

	private class Aim(val box: Box?, val why: String)

	private class Box(val left: Int, val top: Int, val right: Int, val bottom: Int) {
		fun pointX(fraction: Double): Int = (left + (right - left) * fraction).toInt()

		fun pointY(fraction: Double): Int = (top + (bottom - top) * fraction).toInt()
	}

	private companion object {
		const val CENTER = 0.5
		const val DOUBLE_TAP_GAP_MS = 80L
		const val EDGE_MARGIN = 8
		const val STEP_MS = 5L
	}
}
