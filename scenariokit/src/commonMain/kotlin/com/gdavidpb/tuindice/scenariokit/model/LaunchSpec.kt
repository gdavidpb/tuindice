package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

/**
 * How the app is started. The driver never clears state: wiping the app is the
 * harness's job, done before the runner is invoked.
 */
@Serializable
data class LaunchSpec(
	val arguments: Map<String, String>,
	val mockStates: List<MockState> = emptyList()
)
