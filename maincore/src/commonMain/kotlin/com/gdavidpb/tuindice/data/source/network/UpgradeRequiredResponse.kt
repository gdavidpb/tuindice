package com.gdavidpb.tuindice.data.source.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpgradeRequiredResponse(
	@SerialName("code") val code: String,
	@SerialName("minimum_versions") val minimumVersions: MinimumVersions
) {
	@Serializable
	data class MinimumVersions(
		@SerialName("android") val android: Int,
		@SerialName("ios") val ios: Int
	)
}
