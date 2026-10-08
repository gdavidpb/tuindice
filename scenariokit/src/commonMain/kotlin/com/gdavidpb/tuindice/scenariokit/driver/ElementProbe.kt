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
 * screen.
 */
interface ElementProbe {
	/** Polls [q] until it is visible or [timeoutMs] pass. It always looks once, so a timeout of 0 is a single check. */
	fun waitVisible(q: Query, timeoutMs: Long): Boolean

	/** Polls until [q] is not visible; false when [timeoutMs] pass with it still there. Polls like [waitVisible]. */
	fun waitGone(q: Query, timeoutMs: Long): Boolean

	fun isVisible(q: Query): Boolean

	/** False when the element is absent as well as when it is disabled; callers that care check [isVisible] first. */
	fun isEnabled(q: Query): Boolean

	/**
	 * What the element shows, or null when it is absent. For a text field it is meant to be what the user typed (the
	 * iOS driver returns the field's value, "" when it is empty, never its placeholder or label). The interpreter
	 * does not read secure fields back.
	 */
	fun readText(q: Query): String?

	/**
	 * The visible rectangle of [q], or of the whole screen when [q] is null; null when [q] is absent. Android
	 * reports pixels and iOS points: callers compare two reads of the same driver, never values across platforms.
	 */
	fun bounds(q: Query?): ElementBounds?
}
