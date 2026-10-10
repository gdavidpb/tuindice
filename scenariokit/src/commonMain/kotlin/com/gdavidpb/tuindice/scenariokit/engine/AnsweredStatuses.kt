package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.model.Step

/** The responses the backend gave to the route of an `ExpectRequest`, read from the journal of serve events. */
internal object AnsweredStatuses {
	/**
	 * The status of each response the backend gave to the route of [step] (with the credential [header] when given),
	 * most recent first; null where the journal entry has none. `find` returns requests alone, so this reads
	 * `GET /__admin/requests`, which carries each response. The whole result is null when WireMock did not answer.
	 */
	fun of(backend: BackendControl, step: Step.ExpectRequest, header: String?): List<Int?>? {
		val reply = backend.http("GET", "/__admin/requests", null, null)
		if (!reply.isSuccess) return null
		return WireMockJson.array(WireMockJson.obj(reply.body), "requests").filter { event ->
			val request = WireMockJson.child(event, "request")
			WireMockJson.text(request, "method") == step.method &&
				WireMockJson.text(request, "url")?.substringBefore('?') == step.path &&
				(header == null || WireMockJson.header(WireMockJson.child(request, "headers"), "Authorization") == header)
		}.map { WireMockJson.number(WireMockJson.child(it, "response"), "status") }
	}

	/** How many requests to the route the backend answered with the step's status; null when WireMock did not answer. */
	fun count(backend: BackendControl, step: Step.ExpectRequest, header: String?): Int? =
		of(backend, step, header)?.count { it == step.status }

	/** What the route answered instead of [status], most recent first: what the step waited for, and what came. */
	fun describeMissing(backend: BackendControl, step: Step.ExpectRequest, status: Int): String {
		val header = step.basicAuth?.let { BasicAuth.header(it) }
		val answered = of(backend, step, header)?.joinToString { it?.toString() ?: "no status" } ?: "unreadable"
		val times = if (step.atLeast == 1) "" else " at least ${step.atLeast} times"
		return "${step.method} ${step.path} was not answered with $status$times within ${step.timeoutMs} ms; " +
			"it answered, most recent first: $answered"
	}
}
