package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
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

	fun all(): List<DriverContractCheck> = listOfNotNull(
		DriverContractCheck("launch") { problem(driver.launch(fixture.start), "launch returned false") },
		DriverContractCheck("present-element", ::presentElement),
		DriverContractCheck("absent-element", ::absentElement),
		DriverContractCheck("absent-wait-timing", ::absentWaitTiming),
		DriverContractCheck("gone-wait-on-present") {
			problem(!driver.waitGone(present, GONE_WAIT_MS), "waitGone returned true for an element that is on screen")
		},
		fixture.disabledTag?.let { tag ->
			DriverContractCheck("disabled-element") {
				problem(!driver.isEnabled(Query.Tag(tag)), "isEnabled returned true for the disabled element")
			}
		},
		fixture.textFieldTag?.let { tag -> DriverContractCheck("text-entry") { textEntry(Query.Tag(tag)) } },
		DriverContractCheck("foreground") {
			problem(driver.foreground() && driver.isForeground(), "the app is not in the foreground after foreground()")
		},
		DriverContractCheck("backend") {
			val reply = driver.http("GET", "/__admin/scenarios", null, null)
			problem(reply.isSuccess, "GET /__admin/scenarios answered ${reply.status}")
		}
	)

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

	private fun textEntry(field: Query): String? {
		val atomic = driver.setText(field, "ab1") && driver.readText(field) == "ab1"
		val cleared = driver.clearText(field) && driver.readText(field).orEmpty().isEmpty()
		val typed = driver.typeKeys(field, "xy2") && driver.readText(field) == "xy2"
		driver.finishTextEntry()
		return when {
			!atomic -> "setText did not leave the exact text in the field"
			!cleared -> "clearText did not empty the field"
			!typed -> "typeKeys did not leave the exact text in the field"
			else -> null
		}
	}

	private companion object {
		const val GONE_WAIT_MS = 400L
		const val TIMING_PROBE_MS = 600L
		const val TIMING_SLACK_MS = 5_000L
		const val MIN_WAIT_SHARE = 0.8
	}
}
