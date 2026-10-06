package com.gdavidpb.tuindice.scenariokit.model

import com.gdavidpb.tuindice.scenariokit.codec.ResultJson
import com.gdavidpb.tuindice.scenariokit.engine.systemNowIso

/** What a runner gets back; it never throws and carries everything `result.json` needs. */
data class ScenarioOutcome(
	val scenarioId: String,
	val startedAt: String,
	val finishedAt: String,
	val steps: List<StepRecord>,
	val failure: ScenarioFailure?,
	/** Human-readable text: failing step, site, expected against observed, last backend requests. */
	val report: String
) {
	val passed: Boolean get() = failure == null

	val message: String get() = failure?.message.orEmpty()

	/** The exact `result.json` both runners write for this scenario. */
	val resultJson: String get() = ResultJson.encode(this)

	companion object {
		/** An outcome for an exception that escaped the interpreter itself. */
		fun crashed(scenarioId: String, details: String): ScenarioOutcome {
			val now = systemNowIso()
			val failure = ScenarioFailure(
				kind = FailureKind.DRIVER_ERROR,
				stepIndex = -1,
				primitive = "run",
				target = scenarioId,
				message = details,
				expected = "",
				actual = "",
				site = null
			)
			return ScenarioOutcome(scenarioId, now, now, emptyList(), failure, "Scenario $scenarioId crashed: $details")
		}
	}
}
