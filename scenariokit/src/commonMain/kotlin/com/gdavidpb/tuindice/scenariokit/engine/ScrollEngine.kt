package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.describe
import kotlin.math.abs

/**
 * Scrolls until the target is visible and either sits comfortably inside the screen (centre between
 * 15 % and 80 % along the scrolling axis) or one more scroll no longer moves it: the end of the
 * content, or an element that does not belong to the scrolling content (a floating button). "Moves" is
 * counted past [STILL_TOLERANCE] of the screen along the axis: a list at its end still bounces a few
 * points under a swipe (iOS rubber band), while a real scroll moves the element by a third of the screen.
 */
internal class ScrollEngine(private val driver: ScenarioDriver, private val poller: Poller) {
	fun execute(step: Step.ScrollUntilVisible): StepResult {
		var swipeRefused = false
		var beforeSwipe: ElementBounds? = null
		val settledInTime = poller.until(step.timeoutMs, SCROLL_PAUSE_MS) {
			val bounds = driver.bounds(step.q)
			val placement = placement(step.q, step.direction, bounds, beforeSwipe)
			when {
				placement == Placement.SETTLED -> true
				swipeOnce(step.direction) -> {
					beforeSwipe = bounds.takeIf { placement == Placement.OFF_CENTER }
					false
				}
				else -> {
					swipeRefused = true
					true
				}
			}
		}
		// A swipe that began before the deadline is judged by what it did, not by the clock: a swipe can take seconds to be
		// injected on a loaded device (measured: 4 s for a nominal 0.4 s), so the deadline may fall inside it, and the poll
		// never reads the screen again after it. The effect of that last swipe is read once; nothing more is waited for.
		val inView = settledInTime || (!swipeRefused && settledAfterLastSwipe(step, beforeSwipe))
		return when {
			swipeRefused -> StepResult.Failed(FailureKind.ASSERTION, driver.refused("the driver refused the scroll swipe"))
			inView -> StepResult.Passed
			else -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				"${step.q.describe()} did not scroll into view within ${step.timeoutMs} ms"
			)
		}
	}

	private fun settledAfterLastSwipe(step: Step.ScrollUntilVisible, beforeSwipe: ElementBounds?): Boolean =
		placement(step.q, step.direction, driver.bounds(step.q), beforeSwipe) == Placement.SETTLED

	/** [previous] is where the element sat before the last swipe, when it was visible but off-centre. */
	private fun placement(q: Query, direction: Scroll, bounds: ElementBounds?, previous: ElementBounds?): Placement {
		val screen = driver.bounds(null)
		return when {
			!driver.isVisible(q) -> Placement.HIDDEN
			// Visible but without a position (it appeared between the reads, or the driver could not place it): where it
			// sits is unknown, so it is not placed. Scrolling goes on, and the wait ends as a timeout if it never is.
			bounds == null || screen == null -> Placement.HIDDEN
			isCentered(bounds, screen, direction) -> Placement.SETTLED
			previous != null && isStill(previous, bounds, screen, direction) -> Placement.SETTLED
			else -> Placement.OFF_CENTER
		}
	}

	private enum class Placement { HIDDEN, OFF_CENTER, SETTLED }

	private fun isStill(before: ElementBounds, now: ElementBounds, screen: ElementBounds, direction: Scroll): Boolean {
		val vertical = direction == Scroll.ContentDown || direction == Scroll.ContentUp
		val moved = if (vertical) now.centerY - before.centerY else now.centerX - before.centerX
		val extent = if (vertical) screen.bottom - screen.top else screen.right - screen.left
		return abs(moved) <= extent * STILL_TOLERANCE
	}

	private fun isCentered(bounds: ElementBounds, screen: ElementBounds, direction: Scroll): Boolean {
		val vertical = direction == Scroll.ContentDown || direction == Scroll.ContentUp
		val center = if (vertical) bounds.centerY - screen.top else bounds.centerX - screen.left
		val extent = if (vertical) screen.bottom - screen.top else screen.right - screen.left
		return center >= extent * VIEW_MIN && center <= extent * VIEW_MAX
	}

	/** Swipes a third of the screen around its middle; content moving down means the finger goes up. */
	private fun swipeOnce(direction: Scroll): Boolean = when (direction) {
		Scroll.ContentDown -> swipe(MID, MID + HALF_STEP, 0.0, -STEP)
		Scroll.ContentUp -> swipe(MID, MID - HALF_STEP, 0.0, STEP)
		Scroll.ContentForward -> swipe(MID + HALF_STEP, MID, -STEP, 0.0)
		Scroll.ContentBackward -> swipe(MID - HALF_STEP, MID, STEP, 0.0)
	}

	private fun swipe(fx: Double, fy: Double, dx: Double, dy: Double) =
		driver.swipe(null, SwipeVector(fx, fy, dx, dy), SWIPE_MS)

	private companion object {
		const val STILL_TOLERANCE = 0.03
		const val VIEW_MIN = 0.15
		const val VIEW_MAX = 0.80
		const val MID = 0.5
		const val STEP = 0.30
		const val HALF_STEP = 0.15
		const val SWIPE_MS = 400L
		const val SCROLL_PAUSE_MS = 150L
	}
}
