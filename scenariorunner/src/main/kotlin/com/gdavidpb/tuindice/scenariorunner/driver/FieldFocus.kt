package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Gives a text field the focus without touching the screen. A touch lands on a point read from the accessibility
 * tree, and while the keyboard opens that point can be where a key is about to be (the "v" that went into the password
 * field was exactly that), however steady the reads were. A field that already has the focus is left alone; one that
 * has not is asked for it with the accessibility click action, which names the node and no point, so it cannot press a
 * key of the keyboard whatever is in the way.
 */
internal class FieldFocus(private val session: DeviceSession) {
	/** True when [q] holds the focus: it already did (nothing is done), or it took the click action. */
	fun ensure(q: Query): Boolean {
		val already = hasFocus(q)
		session.log.write("typeKeys $q: the field ${if (already) "already has" else "does not have"} the focus")

		return already || request(q)
	}

	private fun hasFocus(q: Query): Boolean =
		runCatching { session.selectors.find(q)?.isFocused == true }.getOrDefault(false)

	/**
	 * Performs the click action of the node once and waits, up to [WAIT_MS], for the field to hold the focus, the
	 * keyboard to be listed and the bounds of both to read the same [STABLE_READS] times in a row. A field that does not
	 * take the focus, or a keyboard that does not show, is a refusal with the state it was left in.
	 */
	private fun request(q: Query): Boolean {
		val node = runCatching { session.selectors.find(q)?.accessibilityNodeInfo }.getOrNull()
		val asked = node != null &&
			runCatching { node.performAction(AccessibilityNodeInfo.ACTION_CLICK) }.getOrDefault(false)

		if (!asked) session.log.refuse("typeKeys", "$q: the field did not take the focus click action")

		var lastPair: Pair<Rect?, Rect?>? = null
		var equal = 0
		val ready = asked && session.poll(WAIT_MS) {
			val pair = session.selectors.find(q)?.visibleBounds to session.keyboard.frame()
			equal = if (pair == lastPair) equal + 1 else 1
			lastPair = pair
			hasFocus(q) && pair.first != null && pair.second != null && equal >= STABLE_READS
		}

		if (asked && !ready) {
			val focused = hasFocus(q)
			val keyboard = session.keyboard.frame()
			val hint = if (focused && keyboard == null) HARDWARE_KEYBOARD_HINT else ""
			val state = "focus $focused, keyboard ${keyboard ?: "not listed"}, field ${lastPair?.first}$hint"
			session.log.refuse("typeKeys", "$q: not ready $WAIT_MS ms after the focus click action ($state)")
		}

		return ready
	}

	private companion object {
		const val WAIT_MS = 3_000L
		const val STABLE_READS = 3
		const val HARDWARE_KEYBOARD_HINT =
			"; the field has the focus and no keyboard came up: a device that reports a hardware keyboard " +
				"may keep the soft one hidden (`hw.keyboard` of the AVD)"
	}
}
