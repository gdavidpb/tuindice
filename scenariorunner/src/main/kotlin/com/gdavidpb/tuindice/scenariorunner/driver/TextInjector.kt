package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import android.view.KeyCharacterMap
import android.view.KeyEvent
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * `typeKeys` clicks the field and injects key events, so the app sees the same input a keyboard would produce;
 * `clearText` assigns the empty text in one accessibility action; `submitTextEntry` injects the Enter key, which a
 * single-line field turns into its IME action, and `hideKeyboard` presses back while the keyboard window is up.
 * Text itself never reaches the driver log (it can be a password): only lengths and counts do.
 */
internal class TextInjector(private val session: DeviceSession) : TextEntry {
	override fun typeKeys(q: Query, text: String): Boolean {
		session.log.clearRefusal()
		val events = keyEventsFor(q, text)
		// Clicks where the field is once it has stopped moving: right after a tap that opens the keyboard
		// the form is still sliding up, and the position read a moment ago is a key of the keyboard.
		val place = if (events != null) session.settledBounds(q) else null

		return events != null && place != null && focus(q, place) && enter(q, text, events)
	}

	/** The key events that spell [text]; null, with the reason in the driver log, if [q] is absent or no key spells it. */
	private fun keyEventsFor(q: Query, text: String): Array<KeyEvent>? {
		val onScreen = session.selectors.find(q) != null
		val map = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
		val events = if (onScreen) map.getEvents(text.toCharArray()) else null

		if (!onScreen) session.log.refuse("typeKeys $q: the field is not on screen")
		if (onScreen && events == null) {
			session.log.refuse("typeKeys $q: the virtual keyboard cannot produce key events for ${text.length} characters")
		}

		return events
	}

	private fun focus(q: Query, place: Rect): Boolean {
		val blocked = session.keyboard.covers(place.centerX(), place.centerY(), "typeKeys focus click on $q")
		val clicked = !blocked &&
			runCatching { session.device.click(place.centerX(), place.centerY()) }.getOrDefault(false)

		if (!blocked && !clicked) session.log.refuse("typeKeys $q: the focus click was not delivered")

		return clicked
	}

	private fun enter(q: Query, text: String, events: Array<KeyEvent>): Boolean {
		val entered = KeyInjector(session).inject(events)

		session.log.write("typeKeys $q: $entered of ${events.size} key events injected for ${text.length} characters")

		if (entered != events.size) {
			val took = "the system took $entered of ${events.size} key events for ${text.length} characters"
			session.log.refuse("typeKeys $q: $took")
		}

		return entered == events.size
	}

	/**
	 * Empties the field with an accessibility action and reads it back: the answer is true only when the field then
	 * reads empty (or has no text at all). `UiObject2.setText` does not throw when the action fails, so the read-back
	 * is the only proof; a label, which cannot be emptied, answers false.
	 */
	override fun clearText(q: Query): Boolean {
		session.log.clearRefusal()
		val field = session.selectors.find(q)
		val assigned = field != null && runCatching { field.text = "" }.isSuccess

		if (!assigned) {
			val why = if (field == null) "the field is not on screen" else "the text could not be assigned"
			session.log.refuse("clearText $q: $why")
		}

		val empty = assigned && session.poll(READ_BACK_MS) {
			session.selectors.find(q) != null && readBack(q).isNullOrEmpty()
		}

		if (assigned && !empty) {
			val left = readBack(q)?.length ?: "no text"
			session.log.refuse("clearText $q: the field is not empty after clearing it; it reads back $left characters")
		}

		return empty
	}

	/**
	 * Sends the Enter key to the focused window. A single-line Compose field performs its IME action on Enter, which
	 * is what the action key of the keyboard does, whether or not the keyboard is showing. The answer says the key was
	 * injected; what the app does with it is the next step's to wait for.
	 */
	override fun submitTextEntry(): Boolean {
		session.log.clearRefusal()
		val sent = KeyInjector(session).press(KeyEvent.KEYCODE_ENTER)

		session.log.write("submitTextEntry: the Enter key ${if (sent) "was injected" else "was not injected"}")
		if (!sent) session.log.refuse("submitTextEntry: the system did not take the Enter key")

		return sent
	}

	/**
	 * True once no keyboard window is showing. Back is pressed only while one is: it closes the keyboard and nothing
	 * else, where with no keyboard it would leave the screen the scenario is on.
	 */
	override fun hideKeyboard(): Boolean {
		session.log.clearRefusal()
		if (session.keyboard.frame() == null) return true

		val pressed = KeyInjector(session).press(KeyEvent.KEYCODE_BACK)
		val gone = pressed && session.poll(HIDE_TIMEOUT_MS) { session.keyboard.frame() == null }

		if (!gone) {
			val why = if (pressed) "the keyboard was still showing $HIDE_TIMEOUT_MS ms after back" else "back was not injected"
			session.log.refuse("hideKeyboard: $why")
		}

		return gone
	}

	private fun readBack(q: Query): String? = session.selectors.find(q)?.let { runCatching { it.text }.getOrNull() }

	private companion object {
		const val HIDE_TIMEOUT_MS = 2_000L
		const val READ_BACK_MS = 2_000L
	}
}
