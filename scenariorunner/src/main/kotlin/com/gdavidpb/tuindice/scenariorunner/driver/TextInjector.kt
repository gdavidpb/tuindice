package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import android.view.KeyCharacterMap
import android.view.KeyEvent
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * `setText` assigns in one accessibility action; `typeKeys` clicks the field and injects
 * key events, so the app sees the same input a keyboard would produce. Text itself never
 * reaches the driver log (it can be a password): only lengths and counts do.
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
	 * Assigns [text] and reads the field back. An empty [text] must leave it empty. A non-empty one must leave
	 * it non-empty and different from what it held, or equal to [text]: the app may transform what it is given
	 * (the USB-ID mask turns `1234567` into `12-34567`), so the exact value is the interpreter's to judge.
	 */
	override fun setText(q: Query, text: String): Boolean {
		val before = readBack(q)
		val assigned = assign(q, text)
		val held = assigned && session.poll(READ_BACK_MS) {
			session.selectors.find(q) != null && leftAsAssigned(readBack(q), before, text)
		}

		if (assigned && !held) {
			val now = readBack(q)?.length ?: "no text"
			session.log.write("setText $q: assigned ${text.length} characters, the field reads back $now")
		}

		return held
	}

	private fun assign(q: Query, text: String): Boolean {
		val field = session.selectors.find(q)
		val done = field != null && runCatching { field.text = text }.isSuccess

		val why = if (field == null) "the field is not on screen" else "the text could not be assigned"

		if (!done) session.log.write("setText $q: $why")

		return done
	}

	override fun clearText(q: Query): Boolean {
		val cleared = setText(q, "")

		if (!cleared) session.log.write("clearText $q: the field is not empty after clearing it")

		return cleared
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

		/** What a field read must look like after `setText(text)`; [before] is the read made before assigning. */
		fun leftAsAssigned(read: String?, before: String?, text: String): Boolean = when {
			text.isEmpty() -> read.isNullOrEmpty()
			else -> !read.isNullOrEmpty() && (read == text || read != before)
		}
	}
}
