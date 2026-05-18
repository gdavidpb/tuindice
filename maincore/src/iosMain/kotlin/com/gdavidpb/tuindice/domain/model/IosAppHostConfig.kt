package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.platform.IosPlatformBridge

data class IosAppHostConfig(
	val bridge: IosPlatformBridge,
	val apiBaseUrl: String,
	val privacyPolicyUrl: String,
	val termsAndConditionsUrl: String,
	val supportUrl: String,
	val debug: Boolean,
	val buildVariant: IosBuildVariant
)
