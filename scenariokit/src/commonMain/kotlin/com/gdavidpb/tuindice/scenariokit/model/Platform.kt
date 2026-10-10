package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Platform {
	@SerialName("android")
	Android,

	@SerialName("ios")
	Ios
}
