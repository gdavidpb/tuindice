package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration

/** Waits for the app to send [method] [path], optionally with `Authorization: Basic` of [basicAuth]. */
fun StepBuilder.expectRequest(
	method: String,
	path: String,
	basicAuth: String? = null,
	timeout: Duration = Timeouts.Wait.asDuration()
) = add(Step.ExpectRequest(method, path, basicAuth, timeout.millis(), site()))
