package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.describe

/**
 * Scrolls until the target sits comfortably inside the screen: visible and with its
 * centre between 15 % and 80 % of the screen along the scrolling axis.
 */
internal class ScrollEngine(private val driver: ScenarioDriver, private val poller: Poller) {
	fun execute(step: Step.ScrollUntilVisible): StepResult {
		var swipeRefused = false
		val inView = poller.until(step.timeoutMs, SCROLL_PAUSE_MS) {
			when {
				isInView(step.q, step.direction) -> true
				swipeOnce(step.direction) -> false
				else -> {
					swipeRefused = true
					true
				}
			}
		}
		return when {
			swipeRefused -> StepResult.Failed(FailureKind.ASSERTION, "the driver refused the scroll swipe")
			inView -> StepResult.Passed
			else -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				"${step.q.describe()} did not scroll into view within ${step.timeoutMs} ms"
			)
		}
	}

	private fun isInView(q: Query, direction: Scroll): Boolean {
		val bounds = driver.bounds(q)
		val screen = driver.bounds(null)
		return when {
			!driver.isVisible(q) -> false
			bounds == null || screen == null -> true
			else -> isCentered(bounds, screen, direction)
		}
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
		const val VIEW_MIN = 0.15
		const val VIEW_MAX = 0.80
		const val MID = 0.5
		const val STEP = 0.30
		const val HALF_STEP = 0.15
		const val SWIPE_MS = 400L
		const val SCROLL_PAUSE_MS = 150L
	}
}
