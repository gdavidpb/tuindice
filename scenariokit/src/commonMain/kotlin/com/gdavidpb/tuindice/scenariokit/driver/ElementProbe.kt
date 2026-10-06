package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/** Read-only view of the screen. Implementations never throw; absent elements yield false or null. */
interface ElementProbe {
	fun waitVisible(q: Query, timeoutMs: Long): Boolean

	fun waitGone(q: Query, timeoutMs: Long): Boolean

	fun isVisible(q: Query): Boolean

	fun isEnabled(q: Query): Boolean

	fun readText(q: Query): String?

	/** Bounds of [q], or of the whole screen when [q] is null. */
	fun bounds(q: Query?): ElementBounds?
}
