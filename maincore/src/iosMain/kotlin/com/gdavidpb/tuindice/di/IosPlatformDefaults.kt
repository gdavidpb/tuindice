package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile

private val IOS_PRODUCTION_DEFAULT_CONFIG = DefaultRemoteConfig.values(
	RemoteConfigDefaultsProfile.PRODUCTION
)

data class IosConfigValues(
	val timeoutMillis: Long = IOS_PRODUCTION_DEFAULT_CONFIG.timeoutMillis,
	val contactEmail: String = IOS_PRODUCTION_DEFAULT_CONFIG.contactEmail,
	val contactSubject: String = IOS_PRODUCTION_DEFAULT_CONFIG.contactSubject,
	val loadingMessages: List<String> = IOS_PRODUCTION_DEFAULT_CONFIG.loadingMessages,
	val updateStalenessDays: Int = IOS_PRODUCTION_DEFAULT_CONFIG.updateStalenessDays,
	val syncsToSuggestReview: Int = IOS_PRODUCTION_DEFAULT_CONFIG.syncsToSuggestReview
)

internal fun iosDefaultConfigValues(buildVariant: IosBuildVariant): IosConfigValues {
	val profile = when (buildVariant) {
		IosBuildVariant.DEBUG -> RemoteConfigDefaultsProfile.DEBUG
		IosBuildVariant.PRODUCTION -> RemoteConfigDefaultsProfile.PRODUCTION
	}

	return DefaultRemoteConfig.values(profile).toIosConfigValues()
}

internal fun IosConfigValues.toDefaultRemoteConfigValues(): DefaultRemoteConfigValues {
	return DefaultRemoteConfigValues(
		timeoutMillis = timeoutMillis,
		contactEmail = contactEmail,
		contactSubject = contactSubject,
		loadingMessages = loadingMessages,
		updateStalenessDays = updateStalenessDays,
		syncsToSuggestReview = syncsToSuggestReview
	)
}

private fun DefaultRemoteConfigValues.toIosConfigValues(): IosConfigValues {
	return IosConfigValues(
		timeoutMillis = timeoutMillis,
		contactEmail = contactEmail,
		contactSubject = contactSubject,
		loadingMessages = loadingMessages,
		updateStalenessDays = updateStalenessDays,
		syncsToSuggestReview = syncsToSuggestReview
	)
}
