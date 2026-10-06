package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.ScenarioFailure

/**
 * The text a failed run prints: failing step, source site, expected against observed,
 * the last backend requests with the `Basic` credential decoded, and every mock
 * scenario that is not in `Started`. "Typed X, backend received Y" lives here.
 */
internal object FailureReport {
	fun build(scenarioId: String, failure: ScenarioFailure, snapshot: BackendSnapshot?): String = buildString {
		appendLine(
			"Scenario $scenarioId failed: ${failure.kind} at step ${failure.stepIndex} (${failure.primitive} ${failure.target})"
		)
		failure.site?.let { appendLine("  at ${it.file}:${it.line}") }
		appendLine("  ${failure.message}")
		if (failure.expected.isNotEmpty() || failure.actual.isNotEmpty()) {
			appendLine("  expected: ${failure.expected}")
			appendLine("  actual:   ${failure.actual}")
		}
		append(backendSection(snapshot))
	}

	private fun backendSection(snapshot: BackendSnapshot?): String = buildString {
		if (snapshot == null) {
			appendLine("Backend: not available")
			return@buildString
		}
		appendLine("Last backend requests (newest first):")
		if (snapshot.requests.isEmpty()) appendLine("  (none)")
		snapshot.requests.forEach { appendLine("  ${describe(it)}") }
		appendLine("Mock scenarios not in Started:")
		if (snapshot.unstarted.isEmpty()) appendLine("  (none)")
		snapshot.unstarted.forEach { appendLine("  ${it.scenario} = ${it.state}") }
		snapshot.error?.let { appendLine("Backend note: $it") }
	}

	private fun describe(entry: JournalEntry): String {
		val credential = BasicAuth.decode(entry.authorization)?.let { " [Basic credential: $it]" }.orEmpty()
		return "${entry.method} ${entry.url} -> ${entry.status ?: "?"}$credential"
	}
}
