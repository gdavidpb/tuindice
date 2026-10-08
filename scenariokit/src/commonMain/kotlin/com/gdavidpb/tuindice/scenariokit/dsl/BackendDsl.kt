package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration

/**
 * Sets the state of the WireMock scenario [scenario] now, in the middle of the scenario
 * (`PUT /__admin/scenarios/{scenario}/state`).
 */
fun StepBuilder.mockState(scenario: String, state: String) = add(Step.SetMockState(scenario, state, site()))

/**
 * Waits for the app to send [method] [path], optionally with `Authorization: Basic` of [basicAuth] and only when the
 * backend answered it with [status].
 */
fun StepBuilder.expectRequest(
	method: String,
	path: String,
	basicAuth: String? = null,
	status: Int? = null,
	timeout: Duration = Timeouts.Wait.asDuration()
) = add(Step.ExpectRequest(method, path, basicAuth, timeout.millis(), status, site()))
