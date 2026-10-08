package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityWindowInfo
import androidx.test.uiautomator.By

/**
 * Knows where the on-screen keyboard is. A touch that lands inside its window presses a key and types a
 * character into the focused field instead of reaching the element the step named, so the gesture is
 * refused and the reason is written to the driver log.
 *
 * The window list of the accessibility service lags behind the window manager: a keyboard that is opening can
 * be missing from it for a while, and a missing keyboard must never read as "no keyboard". So the guard fails
 * closed: when no keyboard window is listed but a text field holds the input focus (a keyboard is showing or about
 * to), it waits up to [LISTING_WAIT_MS] for the window to be listed, and only a keyboard that never appears in that
 * time counts as hidden, which is written to the driver log.
 */
internal class KeyboardGuard(private val session: DeviceSession) {
	/** What one read of the windows says: whether the read worked, and the keyboard window's bounds if it is listed. */
	class Reading(val readable: Boolean, val frame: Rect?)

	/** Bounds of the keyboard window, or null when no keyboard is listed (or the windows cannot be read). */
	fun frame(): Rect? = read().frame

	fun read(): Reading = runCatching {
		val frame = session.instrumentation.uiAutomation.windows
			.firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
			?.let { window -> Rect().also(window::getBoundsInScreen) }
		Reading(true, frame)
	}.getOrDefault(Reading(false, null))

	/**
	 * True when an editable field holds the focus, so a keyboard is showing or opening. It is read from the tree like
	 * every other query: `findFocus(FOCUS_INPUT)` does not find a Compose field (measured: the focused field was in the
	 * tree with `focused="true"` while `findFocus` answered nothing editable).
	 */
	fun textFieldHasFocus(): Boolean = runCatching {
		session.device.hasObject(By.clazz(EDIT_TEXT).focused(true))
	}.getOrDefault(false)

	/** True, after logging why, when the point ([x], [y]) of [gesture] is inside the keyboard window. */
	fun covers(x: Int, y: Int, gesture: String): Boolean {
		var reading = read()

		if (reading.frame == null && textFieldHasFocus()) reading = awaitListing(gesture)

		val keyboard = reading.frame
		val inside = keyboard != null && keyboard.contains(x, y)

		if (inside) {
			session.log.refuse("$gesture at ($x, $y) is inside the on-screen keyboard $keyboard; touch refused")
			SystemClock.sleep(RECHECK_MS)
			session.log.write("keyboard re-read $RECHECK_MS ms after that refusal: ${frame()}")
		}

		return inside
	}

	/** Polls the window list for the keyboard of a focused field; the outcome is written to the driver log. */
	private fun awaitListing(gesture: String): Reading {
		val began = SystemClock.uptimeMillis()
		var reading = read()

		while (reading.frame == null && SystemClock.uptimeMillis() - began < LISTING_WAIT_MS) {
			SystemClock.sleep(LISTING_POLL_MS)
			reading = read()
		}

		val waited = SystemClock.uptimeMillis() - began
		val outcome = if (reading.frame != null) "listed after $waited ms" else "never listed in $waited ms, taken as hidden"
		session.log.write("$gesture: a text field has the focus and no keyboard window was listed; the keyboard was $outcome")

		return reading
	}

	private companion object {
		const val EDIT_TEXT = "android.widget.EditText"
		const val LISTING_WAIT_MS = 1_500L
		const val LISTING_POLL_MS = 100L
		const val RECHECK_MS = 100L
	}
}
