package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.engine.Clocks
import com.gdavidpb.tuindice.scenariokit.engine.Poller
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

/** The probes of text entry: typing, clearing, a long secure run, the keyboard guard and the IME action. */
internal class TextContractChecks(
	private val driver: ScenarioDriver,
	private val clocks: Clocks,
	private val fixture: DriverContractFixture
) {
	fun textEntry(field: Query): String? {
		val wanted = fixture.expectedText
		val firstTyped = driver.typeKeys(field, fixture.textSample)
		val afterFirst = settledText(field) { it == wanted }
		val cleared = driver.clearText(field)
		val afterClear = settledText(field) { it.isNullOrEmpty() }
		val typed = driver.typeKeys(field, fixture.textSample)
		val afterType = settledText(field) { it == wanted }
		return when {
			!firstTyped || afterFirst != wanted ->
				"typeKeys of \"${fixture.textSample}\" left \"$afterFirst\" in the field, expected \"$wanted\""
			!cleared || !afterClear.isNullOrEmpty() -> "clearText left \"$afterClear\" in the field, expected it empty"
			!typed || afterType != wanted ->
				"typeKeys of \"${fixture.textSample}\" left \"$afterType\" in the field, expected \"$wanted\" after clearing it"
			else -> null
		}
	}

	/** A run of at least 25 characters goes in complete: no key dropped or doubled, judged by the field's length. */
	fun longSecureTyping(f: Query): String? {
		val sample = fixture.secureSample
		val typed = driver.typeKeys(f, sample)
		val length = settledText(f) { it?.length == sample.length }?.length
		val cleared = driver.clearText(f)
		return when {
			!typed -> "typeKeys of ${sample.length} characters answered false: ${driver.lastRefusal()}"
			length != sample.length -> "typeKeys of ${sample.length} characters left $length in the secure field"
			!cleared -> "clearText of the secure field answered false: ${driver.lastRefusal()}"
			else -> null
		}
	}

	/**
	 * With the keyboard open, a touch or a swipe that starts on it would press a key. Both are refused with a reason
	 * and the field does not change.
	 */
	fun keyboardGuard(f: Query): String? {
		val typed = driver.typeKeys(f, KEYBOARD_PROBE_TEXT)
		val before = settledText(f) { it?.length == KEYBOARD_PROBE_TEXT.length }
		val touch = driver.tapAt(null, HALF, KEYBOARD_ROW)
		val touchReason = driver.lastRefusal()
		val swipe = driver.swipe(null, SwipeVector(HALF, KEYBOARD_ROW, 0.0, -SWIPE_TRAVEL), SWIPE_PROBE_MS)
		val swipeReason = driver.lastRefusal()
		val after = settledText(f) { it == before }
		val cleared = driver.clearText(f)
		return when {
			!typed -> "typeKeys into the secure field answered false: ${driver.lastRefusal()}"
			touch -> "tapAt on the keyboard returned true"
			touchReason.isNullOrBlank() -> "tapAt on the keyboard was refused without a reason"
			swipe -> "a swipe that starts on the keyboard returned true"
			swipeReason.isNullOrBlank() -> "a swipe that starts on the keyboard was refused without a reason"
			after != before ->
				"the field changed from ${before?.length} to ${after?.length} characters after the refused touches"
			!cleared -> "clearText of the secure field answered false: ${driver.lastRefusal()}"
			else -> null
		}
	}

	/** With a field focused and its keyboard up, the action is sent. */
	fun submitTextEntry(f: Query): String? {
		val typed = driver.typeKeys(f, KEYBOARD_PROBE_TEXT)
		val sent = typed && driver.submitTextEntry()
		return when {
			!typed -> "typeKeys into the secure field answered false: ${driver.lastRefusal()}"
			!sent -> "submitTextEntry answered false with a field focused: ${driver.lastRefusal()}"
			else -> null
		}
	}

	/** Reads the field like the interpreter does: for up to [Timeouts.TextReread], until [accepted] or the time is up. */
	private fun settledText(field: Query, accepted: (String?) -> Boolean): String? {
		var seen: String? = null
		Poller(driver, clocks.timeSource).until(Timeouts.TextReread) {
			seen = driver.readText(field)
			accepted(seen)
		}
		return seen
	}

	private companion object {
		const val HALF = 0.5
		const val SWIPE_TRAVEL = 0.1
		const val SWIPE_PROBE_MS = 300L
		const val KEYBOARD_ROW = 0.9
		const val KEYBOARD_PROBE_TEXT = "abc"
	}
}
