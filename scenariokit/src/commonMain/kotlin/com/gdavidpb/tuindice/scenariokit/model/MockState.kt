package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

/**
 * A WireMock scenario state the interpreter sets before launching the app (`LaunchSpec.mockStates`) or, as a
 * `mockState` step, in the middle of a scenario.
 */
@Serializable
data class MockState(val scenario: String, val state: String)
