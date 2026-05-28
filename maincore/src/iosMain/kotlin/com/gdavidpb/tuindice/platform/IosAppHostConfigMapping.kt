package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.di.IOSContext
import com.gdavidpb.tuindice.domain.model.IosAppHostConfig

fun IosAppHostConfig.toIOSContext(): IOSContext {
	return IOSContext(
		hostCapabilities = bridge.toHostCapabilities(),
		appEnvironment = AppEnvironment(
			apiBaseUrl = apiBaseUrl,
			privacyPolicyUrl = privacyPolicyUrl,
			termsAndConditionsUrl = termsAndConditionsUrl,
			supportUrl = supportUrl,
			debug = debug
		),
		configValues = iosDefaultConfigValues(buildVariant)
	)
}
