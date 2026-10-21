package com.gdavidpb.tuindice.scenariorunner.driver

import android.view.KeyCharacterMap
import android.view.KeyEvent
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * `typeKeys` gives the field the focus and injects key events, so the app sees the same input a keyboard would
 * produce. When the virtual keyboard cannot spell some character of the text (accents, «¿»), the text is assigned
 * to the field in one accessibility action instead and the field is read back, and no key is injected: nothing about
 * its keys is proven, and the interpreter judges the content. `clearText` assigns the empty text the same way;
 * `submitTextEntry` injects the Enter key, which a single-line field turns into its IME action.
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
		val spelling = spellingFor(q, text)
		val before = if (spelling != Spelling.Refused) readBefore(q) else null

		return before != null && focus.ensure(q) && unchangedByFocus(q, before) && when (spelling) {
			is Spelling.Keys -> enter(q, text, spelling.events)
			Spelling.Assign -> assign(q, text, before)
			Spelling.Refused -> false
		}
	}

	/** What the field holds before anything is done to it; null, with the reason in the driver log, if unreadable. */
	private fun readBefore(q: Query): String? = readBack(q).also {
		if (it == null) session.log.refuse("typeKeys", "$q: the field's text could not be read before typing")
	}

	/**
	 * How [text] goes into [q]: as key events when the virtual keyboard spells every character, assigned to the field
	 * when it cannot spell some of them, or refused (with the reason in the driver log) when [q] is absent or no keyboard
	 * map loads.
	 */
	private fun spellingFor(q: Query, text: String): Spelling {
		val onScreen = session.selectors.find(q) != null
		val map = if (onScreen) {
			runCatching { KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD) }.getOrNull()
		} else {
			null
		}

		val refusal = when {
			!onScreen -> "the field is not on screen"
			map == null -> "the virtual keyboard could not be loaded"
			else -> null
		}
		if (refusal != null) {
			session.log.refuse("typeKeys", "$q: $refusal")
			return Spelling.Refused
		}

		return map?.getEvents(text.toCharArray())?.let { events -> Spelling.Keys(events) } ?: Spelling.Assign
	}

	/**
	 * Assigns [text] to the field in one accessibility action, replacing its content, and reads it back: the answer is
	 * true when the field changed after the assignment, that is, when its reading differs from [before] within
	 * [READ_BACK_MS]; the interpreter compares the content, as it does after typed keys.
	 */
	private fun assign(q: Query, text: String, before: String): Boolean {
		val field = session.selectors.find(q)
		val assigned = field != null && runCatching { field.text = text }.isSuccess
		val changed = assigned && session.poll(READ_BACK_MS) { readBack(q)?.let { it != before } == true }

		when {
			changed -> session.log.write(
				"typeKeys $q: ${text.length} characters assigned through accessibility; the keyboard cannot spell some"
			)
			!assigned -> session.log.refuse("typeKeys", "$q: the text could not be assigned")
			else -> {
				val now = readBack(q)?.let { "${it.length} characters" } ?: "no text"
				val reason = "$q: the field did not change after assigning ${text.length} characters (it reads $now)"
				session.log.refuse("typeKeys", reason)
			}
		}

		return changed
	}

	private sealed class Spelling {
		class Keys(val events: Array<KeyEvent>) : Spelling()
		data object Assign : Spelling()
		data object Refused : Spelling()
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
