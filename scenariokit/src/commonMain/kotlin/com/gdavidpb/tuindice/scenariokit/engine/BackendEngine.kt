package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Step
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

	/** The step that sets a mock scenario's state between two steps; a reply that is not 2xx is the backend failing. */
	internal fun setMockState(step: Step.SetMockState): StepResult =
		setState(MockState(step.scenario, step.state))?.let { StepResult.Failed(FailureKind.BACKEND_UNAVAILABLE, it) }
			?: StepResult.Passed

	private fun setState(mock: MockState): String? {
		val path = "/__admin/scenarios/${mock.scenario}/state"
		val body = buildJsonObject { put("state", mock.state) }.toString()
		return describeFailure("PUT", path, backend.http("PUT", path, body, null))
	}

	private fun describeFailure(method: String, path: String, reply: HttpReply): String? =
		if (reply.isSuccess) null else "$method $path answered ${reply.status}: ${reply.body.take(ERROR_BODY_LIMIT)}"

	/**
	 * Waits for the request (with the answer status the step asks for, if any), and on timeout tells a wrong
	 * credential apart from a missing request.
	 */
	internal fun expectRequest(step: Step.ExpectRequest): StepResult {
		val header = step.basicAuth?.let { BasicAuth.header(it) }
		var transportDown = false
		val arrived = poller.until(step.timeoutMs) {
			val count = if (step.status == null) countMatching(step, header) else AnsweredStatuses.count(backend, step, header)
			if (count == null) transportDown = true
			count == null || count >= step.atLeast
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

	/**
	 * The request did not arrive as expected. It is a typing fault only when the app sent the credential it was
	 * given, wrongly: the most recent request to the route carries a Basic credential with the same identifier and
	 * another password, or it is the only request the route has seen. Any other earlier request with another
	 * credential says nothing about this step (the app may have retried with an older password and never sent the new
	 * one), so the step times out and lists every credential the route saw, most recent first.
	 * `find` answers from the oldest request to the most recent (WireMock 3.13.2, proved by
	 * `WireMockJournalOrderTest`), so the list is reversed on reading.
	 */
	private fun missingRequest(step: Step.ExpectRequest): StepResult {
		val pattern = buildJsonObject {
			put("method", step.method)
			put("urlPath", step.path)
		}
		val reply = backend.http("POST", "/__admin/requests/find", pattern.toString(), null)
		val seen = WireMockJson.array(WireMockJson.obj(reply.body), "requests")
			.map { BasicAuth.decode(WireMockJson.header(WireMockJson.child(it, "headers"), "Authorization")) }
			.asReversed()
		val received = seen.firstOrNull()
		return when {
			seen.isEmpty() -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				"no ${step.method} ${step.path} request reached the backend within ${step.timeoutMs} ms"
			)
			step.basicAuth != null && received != step.basicAuth && isTypingFault(step.basicAuth, seen) -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"typed \"${step.basicAuth}\" but the backend received \"${received ?: "no Basic credential"}\" on ${step.target}",
				expected = step.basicAuth,
				actual = received.orEmpty()
			)
			step.status != null -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				AnsweredStatuses.describeMissing(backend, step, step.status)
			)
			else -> StepResult.Failed(
				FailureKind.STEP_TIMEOUT,
				"${step.target} did not reach the backend as expected within ${step.timeoutMs} ms; " +
					"credentials it saw, most recent first: ${seen.joinToString { "\"${it ?: "no Basic credential"}\"" }}"
			)
		}
	}

	/** [seen] is most recent first. */
	private fun isTypingFault(expected: String, seen: List<String?>): Boolean {
		val latest = seen.first()
		val sameIdentifier = latest != null && latest.substringBefore(':') == expected.substringBefore(':')
		return seen.size == 1 || sameIdentifier
	}

	/** Last requests and mock scenarios out of `Started`; transport problems come back in [BackendSnapshot.error]. */
	internal fun snapshot(): BackendSnapshot = BackendSnapshots.read(backend)

	companion object {
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
