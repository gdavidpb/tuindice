package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_SWIPE_MS = 400L
private const val NEAR = 0.2
private const val MIDDLE = 0.5
private const val FAR = 0.8
private const val SHORT_TRAVEL = 0.4
private const val LONG_TRAVEL = 0.6

/** Swipes across the screen, starting where [direction] leaves the most room. */
fun StepBuilder.swipeScreen(direction: SwipeDirection, duration: Duration = DEFAULT_SWIPE_MS.milliseconds) {
	val (fx, fy) = when (direction) {
		SwipeDirection.Up -> MIDDLE to FAR
		SwipeDirection.Down -> MIDDLE to NEAR
		SwipeDirection.Left -> FAR to MIDDLE
		SwipeDirection.Right -> NEAR to MIDDLE
	}
	val travel = if (direction == SwipeDirection.Up || direction == SwipeDirection.Down) SHORT_TRAVEL else LONG_TRAVEL
	swipePoints(fx, fy, direction.dx(travel), direction.dy(travel), duration)
}

/** Swipes starting at the middle of the element, by a screen fraction. */
fun StepBuilder.swipeFrom(tag: String, direction: SwipeDirection, duration: Duration = DEFAULT_SWIPE_MS.milliseconds) =
	add(
		Step.Swipe(
			Query.Tag(tag),
			MIDDLE,
			MIDDLE,
			direction.dx(SHORT_TRAVEL),
			direction.dy(SHORT_TRAVEL),
			duration.millis(),
			site()
		)
	)

/** Swipes from screen fraction (fx, fy) by (dx, dy), also in screen fractions. */
fun StepBuilder.swipePoints(
	fx: Double,
	fy: Double,
	dx: Double,
	dy: Double,
	duration: Duration = DEFAULT_SWIPE_MS.milliseconds
) =
	add(Step.Swipe(null, fx, fy, dx, dy, duration.millis(), site()))

fun StepBuilder.scrollUntilVisible(
	tag: String,
	direction: Scroll = Scroll.ContentDown,
	timeout: Duration = Timeouts.Wait.asDuration()
) = add(Step.ScrollUntilVisible(Query.Tag(tag), direction, timeout.millis(), site()))

private fun SwipeDirection.dx(travel: Double): Double = when (this) {
	SwipeDirection.Left -> -travel
	SwipeDirection.Right -> travel
	else -> 0.0
}

private fun SwipeDirection.dy(travel: Double): Double = when (this) {
	SwipeDirection.Up -> -travel
	SwipeDirection.Down -> travel
	else -> 0.0
}
