package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Touch input. Fractions are of the target's (or the screen's) size; implementations never throw.
 *
 * A gesture answers true when it was delivered, which says nothing about its effect: the next step waits for that.
 * The interpreter waits for the target (visible, and for `tap` enabled) before calling, so a driver may assume the
 * element existed a moment ago. Both drivers aim at where the element is once its bounds stop moving (the keyboard
 * opening, a sheet settling): each reads them until 3 reads in a row agree, at most 20 reads or 8 s (Android reads
 * every 100 ms). When they never do, or the element is no longer there, the gesture is refused: the call
 * answers false, the reason goes to the driver log, and [Diagnostics.lastRefusal] hands it to the step that failed.
 * A touch whose point is inside the on-screen keyboard is refused the same way, on both platforms, because it would
 * press a key and type into the field that has the focus. A touch aimed at an app that is gone answers false, and
 * on iOS an XCTest exception raised while dispatching it is caught and answered false too.
 */
interface Gestures {
	/** Touches the center of the visible part of [q]. */
	fun tap(q: Query): Boolean

	/**
	 * Touches the point at ([fx], [fy]), fractions of the visible area of [q] or of the whole screen when [q] is
	 * null. Use it where no tag reaches the spot, such as a position along a slider.
	 */
	fun tapAt(q: Query?, fx: Double, fy: Double): Boolean

	/** Two touches at the center of [q]. */
	fun doubleTap(q: Query): Boolean

	/**
	 * Drags from the point [SwipeVector.fx], [SwipeVector.fy] of the area of [from] (the screen when it is null)
	 * by [SwipeVector.dx] screen widths and [SwipeVector.dy] screen heights, in [durationMs]. The end point is kept
	 * a few pixels inside the screen edges (8 px on Android, 12 pt on iOS) so that the drag is never an edge
	 * gesture. On iOS the finger lifts at the speed of the drag, as on Android, so the content may fling.
	 */
	fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean

	/**
	 * The system back action. Android injects the key without waiting for the app to consume it and answers true when
	 * it was injected. iOS has no such action and always answers false (saying so as its refusal), so a scenario uses
	 * `back()` only inside `onPlatform(Platform.Android)` or in a scenario restricted to Android.
	 */
	fun pressBack(): Boolean
}
