package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.engine.Clocks
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

/** The negative-behaviour and timing probes every driver must pass before scenarios are trusted on it. */
internal class DriverContractChecks(
	private val fixture: DriverContractFixture,
	private val driver: ScenarioDriver,
	private val clocks: Clocks
) {
	private val present = Query.Tag(fixture.presentTag)
	private val absent = Query.Tag(fixture.absentTag)

	private val texts = TextContractChecks(driver, clocks, fixture)
	private val gestures = GestureContractChecks(driver, fixture)
	private val field = fixture.textFieldTag?.let { Query.Tag(it) }
	private val secure = fixture.secureFieldTag?.takeIf { fixture.secureSample.isNotEmpty() }?.let { Query.Tag(it) }

	fun all(): List<DriverContractCheck> = listOfNotNull(
		DriverContractCheck("launch") { problem(driver.launch(fixture.start), "launch returned false") },
		DriverContractCheck("present-element", ::presentElement),
		DriverContractCheck("absent-element", ::absentElement),
		DriverContractCheck("absent-wait-timing", ::absentWaitTiming),
		DriverContractCheck("gone-wait-on-present") {
			problem(!driver.waitGone(present, GONE_WAIT_MS), "waitGone returned true for an element that is on screen")
		},
		DriverContractCheck("gone-wait-on-absent", ::goneWaitOnAbsent),
		DriverContractCheck("refusals-carry-a-reason", gestures::refusalsCarryAReason),
		fixture.disabledTag?.let { tag -> DriverContractCheck("disabled-element") { disabledElement(Query.Tag(tag)) } },
		field?.let { f -> DriverContractCheck("text-entry") { texts.textEntry(f) } },
		field?.let { f -> DriverContractCheck("positive-gestures") { gestures.positiveGestures(f) } },
		secure?.let { s -> DriverContractCheck("long-secure-typing") { texts.longSecureTyping(s) } },
		secure?.let { s -> DriverContractCheck("keyboard-guard") { texts.keyboardGuard(s) } },
		secure?.let { s -> DriverContractCheck("submit-text-entry") { texts.submitTextEntry(s) } },
		DriverContractCheck("foreground") {
			problem(driver.foreground() && driver.isForeground(), "the app is not in the foreground after foreground()")
		},
		DriverContractCheck("backend") {
			val reply = driver.http("GET", "/__admin/scenarios", null, null)
			problem(reply.isSuccess, "GET /__admin/scenarios answered ${reply.status}")
		},
		DriverContractCheck("back", gestures::back),
		// Last: it ends the app, and nothing after it can use the screen.
		DriverContractCheck("terminated-app", ::terminatedApp)
	)

	/** An absent element is gone at once: the answer is true and quick, on a screen that can be read. */
	private fun goneWaitOnAbsent(): String? {
		val mark = clocks.timeSource.markNow()
		val gone = driver.waitGone(absent, GONE_ABSENT_WAIT_MS)
		val elapsed = mark.elapsedNow().inWholeMilliseconds
		return when {
			!gone -> "waitGone returned false for ${fixture.absentTag}, which is not on screen"
			elapsed > GONE_ABSENT_QUICK_MS -> "waitGone took $elapsed ms to see that ${fixture.absentTag} is not on screen"
			else -> null
		}
	}

	private fun disabledElement(disabled: Query): String? = when {
		!driver.isVisible(disabled) -> "the disabled element is not on screen, so isEnabled proves nothing"
		driver.isEnabled(disabled) -> "isEnabled returned true for the disabled element"
		else -> null
	}

	/**
	 * With the app gone, nothing on it can be touched, typed into or read, and "gone" is not proven by a screen that
	 * cannot be read. Every answer is false, and the runner must come back from each call alive: a touch dispatched to a
	 * dead app is where an XCTest exception used to take the runner down.
	 */
	private fun terminatedApp(): String? {
		driver.terminate()
		val field = fixture.textFieldTag?.let { Query.Tag(it) }
		return when {
			driver.tap(present) -> "tap returned true with the app terminated"
			driver.tapAt(present, HALF, HALF) -> "tapAt returned true with the app terminated"
			driver.doubleTap(present) -> "doubleTap returned true with the app terminated"
			driver.swipe(present, SwipeVector(HALF, HALF, 0.0, -SWIPE_TRAVEL), SWIPE_PROBE_MS) ->
				"swipe returned true with the app terminated"
			field != null && driver.typeKeys(field, fixture.textSample) -> "typeKeys returned true with the app terminated"
			driver.isVisible(present) -> "isVisible is true with the app terminated"
			driver.waitGone(present, GONE_WAIT_MS) -> "waitGone returned true although nothing can be read from a dead app"
			driver.isForeground() -> "isForeground is true with the app terminated"
			driver.foreground() -> "foreground returned true with the app terminated"
			else -> null
		}
	}

	private fun problem(holds: Boolean, message: String): String? = if (holds) null else message

	private fun presentElement(): String? = when {
		!driver.waitVisible(present, Timeouts.Action) -> "waitVisible timed out for ${fixture.presentTag}"
		!driver.isVisible(present) -> "isVisible is false right after waitVisible succeeded"
		driver.bounds(present) == null -> "bounds is null for a visible element"
		else -> null
	}

	private fun absentElement(): String? = when {
		driver.isVisible(absent) -> "isVisible is true for ${fixture.absentTag}"
		driver.isEnabled(absent) -> "isEnabled is true for ${fixture.absentTag}"
		driver.readText(absent) != null -> "readText is not null for ${fixture.absentTag}"
		driver.bounds(absent) != null -> "bounds is not null for ${fixture.absentTag}"
		driver.tap(absent) -> "tap returned true for ${fixture.absentTag}"
		else -> null
	}

	/** A wait for something that never shows must honour its timeout: not return early, not hang. */
	private fun absentWaitTiming(): String? {
		val mark = clocks.timeSource.markNow()
		val shown = driver.waitVisible(absent, TIMING_PROBE_MS)
		val elapsed = mark.elapsedNow().inWholeMilliseconds
		return when {
			shown -> "waitVisible returned true for ${fixture.absentTag}"
			elapsed < TIMING_PROBE_MS * MIN_WAIT_SHARE ->
				"waitVisible returned after $elapsed ms, before its $TIMING_PROBE_MS ms timeout"
			elapsed > TIMING_PROBE_MS + TIMING_SLACK_MS ->
				"waitVisible took $elapsed ms for a $TIMING_PROBE_MS ms timeout"
			else -> null
		}
	}

	private companion object {
		const val GONE_WAIT_MS = 400L
		const val TIMING_PROBE_MS = 600L
		const val TIMING_SLACK_MS = 1_500L
		const val GONE_ABSENT_WAIT_MS = 2_000L
		const val GONE_ABSENT_QUICK_MS = 1_500L
		const val MIN_WAIT_SHARE = 0.8
		const val HALF = 0.5
		const val SWIPE_TRAVEL = 0.1
		const val SWIPE_PROBE_MS = 300L
	}
}
