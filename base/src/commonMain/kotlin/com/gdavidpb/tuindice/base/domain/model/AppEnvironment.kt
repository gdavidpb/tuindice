package com.gdavidpb.tuindice.base.domain.model

data class AppEnvironment(
	val apiBaseUrl: String,
	val privacyPolicyUrl: String,
	val termsAndConditionsUrl: String,
	val supportUrl: String,
	val debug: Boolean
)
