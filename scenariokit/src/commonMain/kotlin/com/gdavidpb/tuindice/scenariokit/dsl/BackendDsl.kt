package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

/**
 * Sets the state of the WireMock scenario [scenario] now, in the middle of the scenario
 * (`PUT /__admin/scenarios/{scenario}/state`).
 */
fun StepBuilder.mockState(scenario: String, state: String) = add(Step.SetMockState(scenario, state, site()))

/**
 * Waits for the app to send [method] [path], optionally with `Authorization: Basic` of [basicAuth] and only when the
 * backend answered it with [status]. The step passes once [atLeast] matching requests are in the backend journal; the
 * journal counts since the start of the scenario, not since this step, so a request sent before it (the app's launch
 * sends some) counts too: to wait for a new request while one is already there, ask for one more.
 */
fun StepBuilder.expectRequest(
	method: String,
	path: String,
	basicAuth: String? = null,
	status: Int? = null,
	atLeast: Int = 1
) = add(Step.ExpectRequest(method, path, basicAuth, Timeouts.Wait.asDuration().millis(), status, atLeast, site()))
