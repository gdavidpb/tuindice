package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

@Serializable
data class Scenario(
	val id: String,
	val module: String,
	val start: LaunchSpec,
	val steps: List<Step>,
	val covers: List<String> = emptyList(),
	val tags: List<String> = emptyList(),
	val platforms: List<Platform> = Platform.entries,
	val account: String? = null,
	val signsIn: Boolean = false,
	val timeoutSeconds: Int = DefaultTimeoutSeconds,
	val quarantine: Quarantine? = null
) {
	companion object {
		const val DefaultTimeoutSeconds = 180
	}
}
