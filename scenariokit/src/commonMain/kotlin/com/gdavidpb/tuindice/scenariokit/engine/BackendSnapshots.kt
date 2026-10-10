package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.model.MockState
import kotlinx.serialization.json.JsonObject

/** Reads the journal and the mock scenarios of WireMock for a failure report. */
internal object BackendSnapshots {
	private const val STARTED = "Started"
	private const val JOURNAL_LIMIT = 5

	/** Last requests and mock scenarios out of `Started`; transport problems come back in [BackendSnapshot.error]. */
	fun read(backend: BackendControl): BackendSnapshot {
		val requests = backend.http("GET", "/__admin/requests?limit=$JOURNAL_LIMIT", null, null)
		val scenarios = backend.http("GET", "/__admin/scenarios", null, null)
		val error = listOf(requests, scenarios).firstOrNull { !it.isSuccess }?.let { "backend unreachable (${it.status})" }
		return BackendSnapshot(
			requests = WireMockJson.array(WireMockJson.obj(requests.body), "requests").map(::journalEntry),
			unstarted = WireMockJson.array(WireMockJson.obj(scenarios.body), "scenarios").mapNotNull(::unstarted),
			error = error
		)
	}

	private fun journalEntry(event: JsonObject): JournalEntry {
		val request = WireMockJson.child(event, "request")
		return JournalEntry(
			method = WireMockJson.text(request, "method").orEmpty(),
			url = WireMockJson.text(request, "url").orEmpty(),
			status = WireMockJson.number(WireMockJson.child(event, "response"), "status"),
			authorization = WireMockJson.header(WireMockJson.child(request, "headers"), "Authorization")
		)
	}

	private fun unstarted(scenario: JsonObject): MockState? {
		val state = WireMockJson.text(scenario, "state")
		val name = WireMockJson.text(scenario, "name")
		return if (name != null && state != null && state != STARTED) MockState(name, state) else null
	}
}
