package com.gdavidpb.tuindice.scenariorunner.driver

import kotlin.math.roundToInt

/**
 * The touch events of one swipe, with the time of each one decided here and not by the clock at the moment of
 * injecting it (E3). It is pure: [plan] only computes the list.
 *
 * `UiDevice.swipe(…, steps)` injected one synchronous event per 5 ms of the nominal duration (80 for a swipe of
 * 0.4 s) and stamped each with the wall clock at the moment it was injected. Every synchronous injection waits for
 * the system, and a swipe was measured to last from 2.7 to 10.6 s instead of 0.4 s; the app, which reads the velocity
 * of the gesture from the stamps, saw a slow drag, and a scroll that depends on it ran out of its deadline. Here the
 * swipe is [MOVES] moves between a down and an up, each stamped at its place of the nominal duration, so the
 * velocity the app sees comes from the stamps and does not depend on how long the host takes to deliver them.
 */
internal object SwipeTrack {
	/** Moves between the down and the up: the swipe is `MOVES + 2` events in all, whatever its duration. */
	const val MOVES = 10

	/** One event of the track: [kind], at ([x], [y]) and stamped [eventTime] (the uptime clock, in ms). */
	data class Sample(val kind: Kind, val x: Float, val y: Float, val eventTime: Long)

	enum class Kind { DOWN, MOVE, UP }

	/**
	 * The samples of a swipe from [start] to [end] (x to y, in pixels) that lasts [durationMs], the first stamped at
	 * [downTime]. The moves are evenly spread over the duration and over the distance; the last move and the up are
	 * at the end point, at `downTime + durationMs`.
	 */
	fun plan(start: Pair<Int, Int>, end: Pair<Int, Int>, durationMs: Long, downTime: Long): List<Sample> {
		val duration = durationMs.coerceAtLeast(MOVES.toLong())
		val moves = (1..MOVES).map { index ->
			val fraction = index.toDouble() / MOVES

			Sample(
				kind = Kind.MOVE,
				x = (start.first + (end.first - start.first) * fraction).toFloat(),
				y = (start.second + (end.second - start.second) * fraction).toFloat(),
				eventTime = downTime + (duration * fraction).roundToInt()
			)
		}

		return listOf(Sample(Kind.DOWN, start.first.toFloat(), start.second.toFloat(), downTime)) + moves +
			Sample(Kind.UP, end.first.toFloat(), end.second.toFloat(), downTime + duration)
	}
}
