package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.model.ScenarioFailure
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome
import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.StepRecord
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The `result.json` each runner writes: `scenarioId`, `outcome` (`passed` or `failed`),
 * `startedAt`, `finishedAt`, `steps[]` and `failure`, which is null when the scenario passed.
 */
internal object ResultJson {
	fun encode(outcome: ScenarioOutcome): String =
		JsonObject(
			linkedMapOf(
				"scenarioId" to JsonPrimitive(outcome.scenarioId),
				"outcome" to JsonPrimitive(if (outcome.passed) "passed" else "failed"),
				"startedAt" to JsonPrimitive(outcome.startedAt),
				"finishedAt" to JsonPrimitive(outcome.finishedAt),
				"steps" to JsonArray(outcome.steps.map(::step)),
				"failure" to (outcome.failure?.let(::failure) ?: JsonNull)
			)
		).toString()

	private fun step(record: StepRecord) = JsonObject(
		linkedMapOf(
			"index" to JsonPrimitive(record.index),
			"primitive" to JsonPrimitive(record.primitive),
			"target" to JsonPrimitive(record.target),
			"durationMs" to JsonPrimitive(record.durationMs),
			"outcome" to JsonPrimitive(record.outcome.wire)
		)
	)

	private fun site(site: Site) = JsonObject(
		mapOf("file" to JsonPrimitive(site.file), "line" to JsonPrimitive(site.line))
	)

	private fun failure(failure: ScenarioFailure) = JsonObject(
		linkedMapOf(
			"kind" to JsonPrimitive(failure.kind.name),
			"stepIndex" to JsonPrimitive(failure.stepIndex),
			"primitive" to JsonPrimitive(failure.primitive),
			"target" to JsonPrimitive(failure.target),
			"message" to JsonPrimitive(failure.message),
			"expected" to JsonPrimitive(failure.expected),
			"actual" to JsonPrimitive(failure.actual),
			"site" to (failure.site?.let(::site) ?: JsonNull)
		)
	)
}
