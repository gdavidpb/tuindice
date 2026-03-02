package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath

data class IosAppHostConfig(
	val bridge: IosPlatformBridge,
	val apiBaseUrl: String,
	val privacyPolicyUrl: String,
	val termsAndConditionsUrl: String,
	val debug: Boolean,
	val buildVariant: IosBuildVariant
)

internal fun IosAppHostConfig.toPlatformConfig(): IosPlatformConfig {
	return IosPlatformConfig(
		hostCapabilities = bridge.toHostCapabilities(),
		appEnvironment = AppEnvironment(
			apiBaseUrl = apiBaseUrl,
			privacyPolicyUrl = privacyPolicyUrl,
			termsAndConditionsUrl = termsAndConditionsUrl,
			debug = debug
		),
		configValues = iosDefaultConfigValues(buildVariant),
		databasePath = defaultIosDatabasePath()
	)
}
