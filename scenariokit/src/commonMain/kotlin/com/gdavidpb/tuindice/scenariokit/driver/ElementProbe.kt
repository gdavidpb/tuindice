package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Read-only view of the screen. Implementations never throw; absent elements yield false or null.
 *
 * How a [Query] finds its element, on both platforms: [Query.Tag] is the `testTag` (Android resource id, iOS
 * accessibility identifier); [Query.Text] is the exact text, or a substring with `contains`, of the element's text
 * (iOS: its label, value or title); [Query.System] is an element of the OS outside the app (Android: resource id or
 * text in any package; iOS: label or identifier in the app or in the springboard). The first match is used. An
 * element counts as present only when it is on screen: on iOS it must also have a non-empty frame that meets the
 * screen, and a tag or a text counts only while the app is in the foreground (its tree stays readable behind Safari
 * or a system sheet, and then it is not what the user sees). On iOS "in the foreground" is the state XCTest has cached
 * for the app, and that is the limit: after the app leaves the front, with Safari opened over it, the cached state
 * keeps saying "foreground" for about 2.7 s (2.56 to 2.89 s, measured against Safari's own state and screenshots), and
 * during that window a lookup that finds the element in the app's tree can answer "on screen" with another app in
 * front. Asking the system with a wait for the background state (`AppControl.isForeground`) sees the change at the same
 * moment as the cached state (within 0.03 s) and costs 0.3 s per hit, so the lookups do not pay it. A scenario that
 * leaves the app on purpose waits for it with `WaitBackgrounded` before it asserts anything about what is on screen.
 */
interface ElementProbe {
	/** Polls [q] until it is visible or [timeoutMs] pass. It always looks once, so a timeout of 0 is a single check. */
	fun waitVisible(q: Query, timeoutMs: Long): Boolean

	/**
	 * Polls until [q] is not visible; false when [timeoutMs] pass with it still there. Polls like [waitVisible].
	 * "Not visible" needs proof that the screen was read in that very round (Android: the root of the app's window is
	 * there and no read threw; iOS: the app is in the foreground and its tree was read): a screen that cannot be read,
	 * or an app that is gone, never counts as "gone", and the poll goes on until the timeout makes it false.
	 */
	fun waitGone(q: Query, timeoutMs: Long): Boolean

	fun isVisible(q: Query): Boolean

	/** False when the element is absent as well as when it is disabled; callers that care check [isVisible] first. */
	fun isEnabled(q: Query): Boolean

	/**
	 * What the element shows, or null when it is absent. For a text field it is meant to be what the user typed (the
	 * iOS driver returns the field's value, "" when it is empty, never its placeholder or label). A secure field
	 * shows one character per typed character on both platforms (Android reads one dot per character, iOS the same
	 * count, measured), so the interpreter compares its length.
	 */
	fun readText(q: Query): String?

	/**
	 * The checked state of the checkbox [q]: true or false for a `toggleable` that is on screen, null when it is absent
	 * or cannot be read. Android reads `checked` of a checkable node, and answers null for a node that is not checkable.
	 * iOS reads a switch, a checkbox, a toggle or a button (that is how Compose publishes a
	 * `toggleable(role = Checkbox)`): the element's value when it has one ("1", "0", "true", "false", "on", "off",
	 * "checked", "unchecked"), otherwise its
	 * `Selected` trait; a value that is present and none of those answers null, never "unchecked". The limit on iOS: an
	 * element that is not a toggle at all but is a button also reads false, so there "unchecked" cannot be told from
	 * "not a toggle". A scenario that asserts a checked state therefore asserts both states of the same element
	 * (`assertChecked(x, true)` after the toggle and `assertChecked(x, false)` before it).
	 */
	fun isChecked(q: Query): Boolean?

	/**
	 * The visible rectangle of [q], or of the whole screen when [q] is null; null when [q] is absent. Android
	 * reports pixels and iOS points: callers compare two reads of the same driver, never values across platforms.
	 */
	fun bounds(q: Query?): ElementBounds?
}
