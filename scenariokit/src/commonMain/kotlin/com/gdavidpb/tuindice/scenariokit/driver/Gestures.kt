package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/** Touch input. Fractions are of the target's (or the screen's) size; implementations never throw. */
interface Gestures {
	fun tap(q: Query): Boolean

	fun tapAt(q: Query?, fx: Double, fy: Double): Boolean

	fun doubleTap(q: Query): Boolean

	fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean

	fun pressBack(): Boolean
}
