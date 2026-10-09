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
		val point = pointOn(q, "tap", "on $q") { it.pointX(CENTER) to it.pointY(CENTER) } ?: return false

		return click(point.first, point.second, "tap", "on $q")
	}

	override fun tapAt(q: Query?, fx: Double, fy: Double): Boolean {
		session.log.clearRefusal()
		val target = q?.toString() ?: "the screen"
		val point = pointOn(q, "tapAt", target) { it.pointX(fx) to it.pointY(fy) } ?: return false

		return click(point.first, point.second, "tapAt", target)
	}

	override fun doubleTap(q: Query): Boolean {
		session.log.clearRefusal()
		val point = pointOn(q, "doubleTap", "on $q") { it.pointX(CENTER) to it.pointY(CENTER) } ?: return false
		val (x, y) = point

		return runCatching {
			val first = session.device.click(x, y)
			SystemClock.sleep(DOUBLE_TAP_GAP_MS)
			session.device.click(x, y) && first
		}.getOrDefault(false).also { if (!it) refuse("doubleTap", "on $q: the clicks were not delivered") }
	}

	override fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean {
		session.log.clearRefusal()
		val screenWidth = session.device.displayWidth
		val screenHeight = session.device.displayHeight
		val source = from ?: "the screen"
		val start = pointOn(from, "swipe", "from $source") { it.pointX(vector.fx) to it.pointY(vector.fy) } ?: return false
		val (startX, startY) = start
		val endX = (startX + vector.dx * screenWidth).toInt().coerceIn(EDGE_MARGIN, screenWidth - EDGE_MARGIN)
		val endY = (startY + vector.dy * screenHeight).toInt().coerceIn(EDGE_MARGIN, screenHeight - EDGE_MARGIN)
		val track = SwipeTrack.plan(start, endX to endY, durationMs, SystemClock.uptimeMillis())

		return runCatching { SwipeInjector(session).inject(track) }.getOrDefault(false)
			.also { if (!it) refuse("swipe", "from $source: the swipe was not delivered") }
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

	/** A click at ([x], [y]), a point that [pointOn] has already cleared with the keyboard guard. */
	private fun click(x: Int, y: Int, primitive: String, target: String): Boolean =
		runCatching { session.device.click(x, y) }.getOrDefault(false)
			.also { if (!it) refuse(primitive, "$target at ($x, $y): the click was not delivered") }

	/**
	 * The point of a touch for [primitive] aimed at [q] (the screen when null), given by [at] on the box of the element,
	 * or null when the touch is refused (the reason is in the driver log): the element is not on screen or does not
	 * settle, or the keyboard guard refuses the point because it is inside the keyboard on screen, where it would
	 * press a key.
	 *
	 * When the guard had to wait for the keyboard to be listed, the form was moving while it opened, and the point
	 * computed before the wait may be where the element no longer is (YB-4). The bounds are then read again, once, and
	 * the point is computed on them; the gesture is still made once. A touch on the screen has no element to read again.
	 */
	private fun pointOn(q: Query?, primitive: String, target: String, at: (Box) -> Pair<Int, Int>): Pair<Int, Int>? {
		val gesture = "$primitive $target"
		val first = boxOf(q, primitive) ?: return null
		val check = session.keyboard.check(gesture)
		val box = if (check.waited && q != null) boxOf(q, primitive) else first
		val point = box?.let(at)

		return point?.takeUnless { session.keyboard.blocks(check, it.first, it.second, gesture) }
	}

	/** The box [q] occupies (the screen when null), or null after writing why the gesture on it is refused. */
	private fun boxOf(q: Query?, primitive: String): Box? {
		val aim = area(q)

		if (aim.box == null) refuse(primitive, aim.why)

		return aim.box
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
	}
}
