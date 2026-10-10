package com.gdavidpb.tuindice.scenariorunner.driver

import android.view.KeyCharacterMap
import android.view.KeyEvent
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * `typeKeys` gives the field the focus and injects key events, so the app sees the same input a keyboard would
 * produce; `clearText` assigns the empty text in one accessibility action; `submitTextEntry` injects the Enter key,
 * which a single-line field turns into its IME action.
 * Text itself never reaches the driver log (it can be a password): only lengths and counts do.
 *
 * The focus is never given with a touch. A touch lands on a point read from the accessibility tree, and while the
 * keyboard opens that point can be where a key of the keyboard is about to be (the "v" that went into the password
 * field was exactly that), however steady the reads were. A field that already has the focus is left alone; one that
 * has not is asked for it with the accessibility click action, which names the node and no point, so it cannot press a
 * key of the keyboard whatever is in the way.
 */
internal class TextInjector(private val session: DeviceSession) : TextEntry {
	private val focus = FieldFocus(session)

	@Volatile
	private var injected = 0

	override fun keysInjected(): Int = injected

	override fun typeKeys(q: Query, text: String): Boolean {
		session.log.clearRefusal()
		injected = 0
		val events = keyEventsFor(q, text)
		val before = if (events != null) readBefore(q) else null

		return events != null && before != null &&
			focus.ensure(q) && unchangedByFocus(q, before) && enter(q, text, events)
	}

	/** What the field holds before anything is done to it; null, with the reason in the driver log, if unreadable. */
	private fun readBefore(q: Query): String? = readBack(q).also {
		if (it == null) session.log.refuse("typeKeys", "$q: the field's text could not be read before typing")
	}

	/** The key events that spell [text]; null, with the reason in the driver log, if [q] is absent or no key spells it. */
	private fun keyEventsFor(q: Query, text: String): Array<KeyEvent>? {
		val onScreen = session.selectors.find(q) != null
		val map = runCatching { KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD) }.getOrNull()
		val events = if (onScreen && map != null) map.getEvents(text.toCharArray()) else null

		if (!onScreen) session.log.refuse("typeKeys", "$q: the field is not on screen")
		if (onScreen && map == null) session.log.refuse("typeKeys", "$q: the virtual keyboard could not be loaded")
		if (onScreen && map != null && events == null) {
			session.log.refuse("typeKeys", "$q: the virtual keyboard cannot produce key events for ${text.length} characters")
		}

		return events
	}

	/** Asking for the focus must not alter the field: if it holds other text than before, nothing is typed. */
	private fun unchangedByFocus(q: Query, before: String): Boolean {
		val now = readBack(q)
		val unchanged = now == before

		if (!unchanged) {
			val held = "it held ${before.length} characters and now holds ${now?.length}"
			session.log.refuse("typeKeys", "$q: the focus request changed the field ($held)")
		}

		return unchanged
	}

	private fun enter(q: Query, text: String, events: Array<KeyEvent>): Boolean {
		val result = KeyInjector(session).inject(events)
		injected = result.entered

		val counted = "${result.entered} of ${events.size} key events injected for ${text.length} characters " +
			"(slowest injection ${result.slowestMs} ms)"
		session.log.write("typeKeys $q: $counted")

		if (result.entered != events.size) {
			val took = "the system took ${result.entered} of ${events.size} key events for ${text.length} characters; " +
				"the refused injection answered after ${result.refusedAfterMs} ms"
			session.log.refuse("typeKeys", "$q: $took")
		}

		return result.entered == events.size
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
			session.log.refuse("clearText", "$q: $why")
		}

		val empty = assigned && session.poll(READ_BACK_MS) {
			session.selectors.find(q) != null && readBack(q).isNullOrEmpty()
		}

		if (assigned && !empty) {
			val left = readBack(q)?.length ?: "no text"
			session.log.refuse("clearText", "$q: the field is not empty after clearing it; it reads back $left characters")
		}

		return empty
	}

	/**
	 * Sends the Enter key to the focused field, as the action key of its keyboard does. Right after a touch that
	 * opens the keyboard the keyboard and the focus are not there yet, so it waits, as a condition, up to
	 * [SUBMIT_WAIT_MS] (`Timeouts.Action`) for a keyboard window and a text field with the focus; when they do not come
	 * it answers false with what it saw. A single-line Compose field performs its IME action on Enter. The answer says
	 * the key was injected; what the app does with it is the next step's to wait for.
	 */
	override fun submitTextEntry(): Boolean {
		session.log.clearRefusal()
		val ready = session.poll(SUBMIT_WAIT_MS) { session.keyboard.frame() != null && session.keyboard.textFieldHasFocus() }

		if (!ready) {
			val keyboard = session.keyboard.frame() ?: "not listed"
			val reason = "no keyboard and focused text field within $SUBMIT_WAIT_MS ms (keyboard $keyboard)"
			session.log.refuse("submitTextEntry", reason)
		}

		val sent = ready && KeyInjector(session).press(KeyEvent.KEYCODE_ENTER)

		if (ready) {
			session.log.write("submitTextEntry: the Enter key ${if (sent) "was injected" else "was not injected"}")
			if (!sent) session.log.refuse("submitTextEntry", "the system did not take the Enter key")
		}

		return sent
	}

	private fun readBack(q: Query): String? = session.selectors.find(q)?.let { runCatching { it.text }.getOrNull() }

	private companion object {
		const val SUBMIT_WAIT_MS = 10_000L
		const val READ_BACK_MS = 2_000L
	}
}
