package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import android.view.accessibility.AccessibilityWindowInfo

/**
 * Knows where the on-screen keyboard is. A touch that lands inside its window presses a key and types a
 * character into the focused field instead of reaching the element the step named, so the gesture is
 * refused and the reason is written to the driver log.
 */
internal class KeyboardGuard(private val session: DeviceSession) {
	/** Bounds of the keyboard window, or null when no keyboard is showing (or the windows cannot be read). */
	fun frame(): Rect? = runCatching {
		session.instrumentation.uiAutomation.windows
			.firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
			?.let { window -> Rect().also(window::getBoundsInScreen) }
	}.getOrNull()

	/** True, after logging why, when the point ([x], [y]) of [gesture] is inside the keyboard window. */
	fun covers(x: Int, y: Int, gesture: String): Boolean {
		val keyboard = frame()
		val inside = keyboard != null && keyboard.contains(x, y)

		if (inside) session.log.refuse("$gesture at ($x, $y) is inside the on-screen keyboard $keyboard; touch refused")

		return inside
	}
}
