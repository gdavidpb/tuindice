package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query

/** The probes of gestures: refusals that say why, gestures that work, and Back. */
internal class GestureContractChecks(
	private val driver: ScenarioDriver,
	private val fixture: DriverContractFixture
) {
	private val absent = Query.Tag(fixture.absentTag)

	/**
	 * Every gesture and text call aimed at an element that is not on screen answers false and says why; a call that
	 * works leaves no reason behind (each call forgets the last one).
	 */
	fun refusalsCarryAReason(): String? {
		val calls: List<Pair<String, () -> Boolean>> = listOf(
			"tap" to { driver.tap(absent) },
			"tapAt" to { driver.tapAt(absent, HALF, HALF) },
			"doubleTap" to { driver.doubleTap(absent) },
			"swipe" to { driver.swipe(absent, SwipeVector(HALF, HALF, 0.0, -SWIPE_TRAVEL), SWIPE_PROBE_MS) },
			"typeKeys" to { driver.typeKeys(absent, "a") },
			"clearText" to { driver.clearText(absent) }
		)
		return calls.firstNotNullOfOrNull { (name, call) ->
			when {
				call() -> "$name returned true for ${fixture.absentTag}"
				driver.lastRefusal().isNullOrBlank() -> "$name refused ${fixture.absentTag} without a reason"
				else -> null
			}
		}
	}

	/**
	 * A gesture that works answers true and leaves no refusal. They are aimed at the text field, which is above the
	 * keyboard once it is focused, and they do nothing the screen could not take twice.
	 */
	fun positiveGestures(f: Query): String? {
		val calls: List<Pair<String, () -> Boolean>> = listOf(
			"tap" to { driver.tap(f) },
			"tapAt" to { driver.tapAt(f, HALF, HALF) },
			"doubleTap" to { driver.doubleTap(f) },
			"swipe" to { driver.swipe(f, SwipeVector(HALF, HALF, 0.0, -SWIPE_TRAVEL), SWIPE_PROBE_MS) }
		)
		return calls.firstNotNullOfOrNull { (name, call) ->
			when {
				!call() -> "$name returned false for the text field: ${driver.lastRefusal()}"
				driver.lastRefusal() != null -> "$name worked but left the refusal \"${driver.lastRefusal()}\""
				else -> null
			}
		}
	}

	/** Back is Android's; iOS has no such action and says so. Last before the app is ended: Back may leave the app. */
	fun back(): String? {
		val answer = driver.pressBack()
		return when {
			driver.platform == Platform.Android && !answer -> "pressBack answered false on Android: ${driver.lastRefusal()}"
			driver.platform == Platform.Ios && answer -> "pressBack answered true on iOS, which has no back action"
			driver.platform == Platform.Ios && driver.lastRefusal().isNullOrBlank() -> "pressBack was refused without a reason"
			else -> null
		}
	}

	private companion object {
		const val HALF = 0.5
		const val SWIPE_TRAVEL = 0.1
		const val SWIPE_PROBE_MS = 300L
	}
}
