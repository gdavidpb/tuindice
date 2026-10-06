package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Everything the interpreter does against WireMock: the per-scenario reset and mock
 * states, the request checks and the journal snapshot for failure reports. It is the
 * only place that resets the backend.
 */
class BackendEngine internal constructor(private val backend: BackendControl, private val poller: Poller) {
	/** Resets WireMock and sets the scenario's mock states; the failure message, or null when ready. */
	internal fun prepare(start: LaunchSpec): String? =
		resetPaths.firstNotNullOfOrNull { reset ->
			describeFailure(reset.method, reset.path, backend.http(reset.method, reset.path, null, reset.authorization))
		} ?: start.mockStates.firstNotNullOfOrNull { setState(it) }

	private fun setState(mock: MockState): String? {
		val path = "/__admin/scenarios/${mock.scenario}/state"
		val body = buildJsonObject { put("state", mock.state) }.toString()
		return describeFailure("PUT", path, backend.http("PUT", path, body, null))
	}

	private fun describeFailure(method: String, path: String, reply: HttpReply): String? =
		if (reply.isSuccess) null else "$method $path answered ${reply.status}: ${reply.body.take(ERROR_BODY_LIMIT)}"

	/** Waits for the request, and on timeout tells a wrong credential apart from a missing request. */
	internal fun expectRequest(step: Step.ExpectRequest): StepResult {
		val header = step.basicAuth?.let { BasicAuth.header(it) }
		var transportDown = false
		val arrived = poller.until(step.timeoutMs) {
			val count = countMatching(step, header)
			if (count == null) transportDown = true
			count == null || count >= 1
		}
		return when {
			transportDown -> StepResult.Failed(
				FailureKind.BACKEND_UNAVAILABLE,
				"WireMock did not answer while waiting for ${step.target}"
			)
			arrived -> StepResult.Passed
			else -> missingRequest(step)
		}
	}

	private fun countMatching(step: Step.ExpectRequest, header: String?): Int? {
		val pattern = buildJsonObject {
			put("method", step.method)
			put("urlPath", step.path)
			if (header != null) {
				putJsonObject("headers") { putJsonObject("Authorization") { put("equalTo", header) } }
			}
		}
		val reply = backend.http("POST", "/__admin/requests/count", pattern.toString(), null)
		return if (reply.isSuccess) WireMockJson.number(WireMockJson.obj(reply.body), "count") else null
	}

	private fun missingRequest(step: Step.ExpectRequest): StepResult {
		val pattern = buildJsonObject {
			put("method", step.method)
			put("urlPath", step.path)
		}
		val reply = backend.http("POST", "/__admin/requests/find", pattern.toString(), null)
		val last = WireMockJson.array(WireMockJson.obj(reply.body), "requests").firstOrNull()
		val received = BasicAuth.decode(WireMockJson.header(WireMockJson.child(last, "headers"), "Authorization"))
		return when {
			last == null -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				"no ${step.target} request reached the backend within ${step.timeoutMs} ms"
			)
			step.basicAuth != null && received != step.basicAuth -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"typed \"${step.basicAuth}\" but the backend received \"${received ?: "no Basic credential"}\" on ${step.target}",
				expected = step.basicAuth,
				actual = received.orEmpty()
			)
			else -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				"${step.target} reached the backend but not as expected within ${step.timeoutMs} ms"
			)
		}
	}

	/** Last requests and mock scenarios out of `Started`; transport problems come back in [BackendSnapshot.error]. */
	internal fun snapshot(): BackendSnapshot {
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

	companion object {
		private const val STARTED = "Started"
		private const val JOURNAL_LIMIT = 5
		private const val ERROR_BODY_LIMIT = 200

		/** Every request that returns WireMock and its custom transformers to a known state. */
		val resetPaths: List<ResetRequest> = listOf(
			ResetRequest("POST", "/__admin/scenarios/reset", null),
			ResetRequest("DELETE", "/__admin/requests", null),
			ResetRequest("POST", "/evaluations/v3/reset", HARNESS_RESET_AUTHORIZATION),
			ResetRequest("POST", "/record/v5/reset", HARNESS_RESET_AUTHORIZATION)
		)
	}
}
