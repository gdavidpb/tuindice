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
 * be missing from it for a while, and a missing keyboard must never read as "no keyboard". So when no keyboard
 * window is listed but a text field holds the input focus (a keyboard is showing or about to), the guard asks the
 * input method itself (`dumpsys input_method`, [InputMethodDump]), which does not lag. What it decides from the
 * readings is [GuardVerdict], a table with a row for each case:
 *
 * - the input method says the keyboard is hidden (a field keeps the focus after Back closed it): there is nothing to
 *   wait for and the touch goes on at once;
 * - it says the keyboard is shown: the guard waits up to [LISTING_WAIT_MS] for the window to be listed, and a window
 *   that is still not listed then, with the input method still saying shown, is a refusal (the touch could press a
 *   key);
 * - it cannot be read: the guard has no other source, waits the same time and then takes the keyboard as hidden. That
 *   is the one place where it lets a touch through on a guess, and it is written to the driver log and counted as a
 *   tolerance (`keyboard-taken-as-hidden`).
 *
 * A window list that cannot be read at all is not "no keyboard" either: if the input method says the keyboard is
 * shown the touch is refused; otherwise the touch goes on, written to the log and counted (`keyboard-unreadable`).
 * A focus that cannot be read is asked about as if a field had it. Every touch refused by the guard is a `guard`
 * refusal.
 *
 * When the guard had to wait for the keyboard to be listed, the form was moving while it opened: [Check.waited] says
 * so, and a gesture aimed at an element reads the element's bounds again before it touches (YB-4).
 */
internal class KeyboardGuard(private val session: DeviceSession) {
	/** What one read of the windows says: whether the read worked, and the keyboard window's bounds if it is listed. */
	class Reading(val readable: Boolean, val frame: Rect?)

	/**
	 * What the guard found for a touch, before the point is looked at: whether it refuses at once, whether it had to
	 * wait for the keyboard to be listed ([waited]: the layout has moved since the element's bounds were read) and the
	 * keyboard window it saw.
	 */
	class Check(val refused: Boolean, val waited: Boolean, val frame: Rect?)

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
	 * every other query: `findFocus(FOCUS_INPUT)` does not find a Compose field (measured: the focused field was in
	 * the tree with `focused="true"` while `findFocus` answered nothing editable).
	 */
	fun textFieldHasFocus(): Boolean = fieldFocus() == true

	/** [textFieldHasFocus], or null when the tree could not be read: nothing is known about the focus then. */
	private fun fieldFocus(): Boolean? = runCatching {
		session.device.hasObject(By.clazz(EDIT_TEXT).focused(true))
	}.getOrNull()

	/** True, after logging why, when the touch of [gesture] at ([x], [y]) must not be made. */
	fun covers(x: Int, y: Int, gesture: String): Boolean = blocks(check(gesture), x, y, gesture)

	/** True, after logging why, when [check] refused the touch or the keyboard window it saw holds ([x], [y]). */
	fun blocks(check: Check, x: Int, y: Int, gesture: String): Boolean {
		val keyboard = check.frame
		val inside = keyboard != null && keyboard.contains(x, y)

		if (inside) {
			session.log.refuse("guard", "$gesture at ($x, $y) is inside the on-screen keyboard $keyboard; touch refused")
		}

		return inside || check.refused
	}

	/** The verdict of the guard for a touch of [gesture]: it reads, and waits for the keyboard when one may be opening. */
	fun check(gesture: String): Check {
		val reading = read()
		val shown = lazy { session.inputMethodShown() }
		val verdict = GuardVerdict.beforeWaiting(reading.readable, reading.frame != null, ::fieldFocus) { shown.value }

		return when (verdict) {
			GuardVerdict.REFUSE -> {
				val why = "the windows could not be read and the input method says the keyboard is shown; touch refused"
				session.log.refuse("guard", "$gesture: $why")
				Check(refused = true, waited = false, frame = null)
			}
			GuardVerdict.PASS_UNREADABLE -> {
				val said = "input method says ${describe(shown.value)}"
				val detail = "$gesture: the keyboard windows could not be read ($said); taken as hidden"
				session.log.tolerate("keyboard-unreadable", detail)
				Check(refused = false, waited = false, frame = null)
			}
			GuardVerdict.PASS_HIDDEN -> {
				val said = "a text field has the focus and the input method says the keyboard is hidden; not waited for"
				session.log.write("guard: $gesture: $said")
				Check(refused = false, waited = false, frame = null)
			}
			GuardVerdict.WAIT -> waitForListing(gesture, shown.value)
			else -> Check(refused = false, waited = false, frame = reading.frame)
		}
	}

	/** A field has the focus and no keyboard window is listed: wait for it, then decide with the input method. */
	private fun waitForListing(gesture: String, shownBefore: Boolean?): Check {
		val began = SystemClock.uptimeMillis()
		var reading = read()

		while (reading.frame == null && SystemClock.uptimeMillis() - began < LISTING_WAIT_MS) {
			SystemClock.sleep(LISTING_POLL_MS)
			reading = read()
		}

		val waited = SystemClock.uptimeMillis() - began
		val seen = "a text field has the focus, no keyboard window was listed and the input method says " +
			describe(shownBefore)
		val verdict = GuardVerdict.afterWaiting(reading.frame != null, shownBefore) { session.inputMethodShown() }

		return when (verdict) {
			GuardVerdict.PASS_AFTER_WAIT -> {
				session.log.write("guard: $gesture: $seen; the keyboard was listed after $waited ms")
				Check(refused = false, waited = true, frame = reading.frame)
			}
			GuardVerdict.PASS_TAKEN_AS_HIDDEN -> {
				session.log.tolerate("keyboard-taken-as-hidden", "$gesture: $seen; the keyboard was never listed in $waited ms")
				Check(refused = false, waited = false, frame = null)
			}
			GuardVerdict.PASS_HIDDEN -> {
				session.log.write("guard: $gesture: the keyboard was hidden while it was waited for ($waited ms); not refused")
				Check(refused = false, waited = false, frame = null)
			}
			else -> {
				val why = "the input method says the keyboard is shown but its window was not listed in $waited ms; touch refused"
				session.log.refuse("guard", "$gesture: $why")
				Check(refused = true, waited = false, frame = null)
			}
		}
	}

	private fun describe(shown: Boolean?) = when (shown) {
		true -> "shown"
		false -> "hidden"
		null -> "nothing readable"
	}

	private companion object {
		const val EDIT_TEXT = "android.widget.EditText"
		const val LISTING_WAIT_MS = 1_500L
		const val LISTING_POLL_MS = 100L
	}
}
