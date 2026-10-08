package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import android.view.KeyCharacterMap
import android.view.KeyEvent
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * `typeKeys` clicks the field and injects key events, so the app sees the same input a keyboard would produce;
 * `clearText` assigns the empty text in one accessibility action. Text itself never reaches the driver log
 * (it can be a password): only lengths and counts do.
 */
internal class TextInjector(private val session: DeviceSession) : TextEntry {
	override fun typeKeys(q: Query, text: String): Boolean {
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

		if (!onScreen) session.log.write("typeKeys $q: the field is not on screen")
		if (onScreen && events == null) {
			session.log.write("typeKeys $q: the virtual keyboard cannot produce key events for ${text.length} characters")
		}

		return events
	}

	private fun focus(q: Query, place: Rect): Boolean {
		val blocked = session.keyboard.covers(place.centerX(), place.centerY(), "typeKeys focus click on $q")
		val clicked = !blocked &&
			runCatching { session.device.click(place.centerX(), place.centerY()) }.getOrDefault(false)

		if (!blocked && !clicked) session.log.write("typeKeys $q: the focus click was not delivered")

		return clicked
	}

	private fun enter(q: Query, text: String, events: Array<KeyEvent>): Boolean {
		val entered = KeyInjector(session).inject(events)

		session.log.write("typeKeys $q: $entered of ${events.size} key events injected for ${text.length} characters")

		return entered == events.size
	}

	/**
	 * Empties the field with an accessibility action and reads it back: the answer is true only when the field then
	 * reads empty (or has no text at all). `UiObject2.setText` does not throw when the action fails, so the read-back
	 * is the only proof; a label, which cannot be emptied, answers false.
	 */
	override fun clearText(q: Query): Boolean {
		val field = session.selectors.find(q)
		val assigned = field != null && runCatching { field.text = "" }.isSuccess

		if (!assigned) {
			val why = if (field == null) "the field is not on screen" else "the text could not be assigned"
			session.log.write("clearText $q: $why")
		}

		val empty = assigned && session.poll(READ_BACK_MS) {
			session.selectors.find(q) != null && readBack(q).isNullOrEmpty()
		}

		if (assigned && !empty) {
			val left = readBack(q)?.length ?: "no text"
			session.log.write("clearText $q: the field is not empty after clearing it; it reads back $left characters")
		}

		return empty
	}

	override fun finishTextEntry(): Boolean {
		if (!keyboardShown()) return true

		session.device.pressBack()

		return session.poll(HIDE_TIMEOUT_MS) { !keyboardShown() }
	}

	private fun readBack(q: Query): String? = session.selectors.find(q)?.let { runCatching { it.text }.getOrNull() }

	private fun keyboardShown(): Boolean = session.shell("dumpsys input_method").contains("mInputShown=true")

	internal companion object {
		private const val HIDE_TIMEOUT_MS = 2_000L
		private const val READ_BACK_MS = 2_000L
	}
}
