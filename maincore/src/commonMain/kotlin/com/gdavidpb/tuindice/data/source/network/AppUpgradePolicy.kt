package com.gdavidpb.tuindice.data.source.network

import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal object AppUpgradePolicy {
	const val OUTDATED_APP_CODE = "outdated_app"

	fun toOutdatedAppState(
		response: UpgradeRequiredResponse,
		userAgentValue: String?
	): OutdatedAppState? {
		if (response.code != OUTDATED_APP_CODE) return null

		val minimumVersionCode = response.minimumVersions.minimumForUserAgent(userAgentValue)

		return OutdatedAppState(minimumVersionCode = minimumVersionCode)
	}

	private fun UpgradeRequiredResponse.MinimumVersions.minimumForUserAgent(userAgentValue: String?): Long {
		val platform = userAgentValue
			?.split(';')
			?.getOrNull(USER_AGENT_PLATFORM_INDEX)

		return when (platform) {
			PLATFORM_ANDROID -> android.toLong()
			PLATFORM_IOS -> ios.toLong()
			else -> maxOf(android, ios).toLong()
		}
	}

	private const val USER_AGENT_PLATFORM_INDEX = 3
	private const val PLATFORM_ANDROID = "Android"
	private const val PLATFORM_IOS = "iOS"
}

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
