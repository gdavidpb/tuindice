package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.engine.Clocks
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.ScenarioFailure
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome
import com.gdavidpb.tuindice.scenariokit.model.StepRecord

/** Runs [DriverContractChecks] in order against a driver and reports them like a scenario. */
internal object DriverContract {
	const val ID = "driver-contract"

	/** Runs every check, even after a failure, so one run reports everything that is wrong. */
	fun run(fixture: DriverContractFixture, driver: ScenarioDriver, clocks: Clocks): ScenarioOutcome {
		val startedAt = clocks.nowIso()
		val records = mutableListOf<StepRecord>()
		val failures = mutableListOf<ScenarioFailure>()
		for ((index, check) in DriverContractChecks(fixture, driver, clocks).all().withIndex()) {
			val mark = clocks.timeSource.markNow()
			val result = runCatching { check.run() }
			val problem = result.getOrNull()
			val broken = problem != null || result.isFailure
			val outcome = if (broken) StepOutcome.Failed else StepOutcome.Passed
			records += StepRecord(index, check.name, "", mark.elapsedNow().inWholeMilliseconds, outcome)
			if (broken) failures += failureOf(index, check, problem, result.exceptionOrNull())
		}
		val failure = failures.firstOrNull()?.copy(message = failures.joinToString("\n") { "${it.primitive}: ${it.message}" })
		val report = failure?.let { "Driver contract failed (${failures.size} of ${records.size} checks):\n${it.message}" }
			?: "Driver contract passed"
		return ScenarioOutcome(ID, startedAt, clocks.nowIso(), records, failure, report)
	}

	private fun failureOf(index: Int, check: DriverContractCheck, problem: String?, error: Throwable?) =
		ScenarioFailure(
			kind = if (error != null) FailureKind.DRIVER_ERROR else FailureKind.ASSERTION,
			stepIndex = index,
			primitive = check.name,
			target = "",
			message = problem ?: "the driver threw: ${error?.stackTraceToString()?.take(ERROR_LIMIT)}",
			expected = "",
			actual = "",
			site = null
		)

	private const val ERROR_LIMIT = 2_000
}
