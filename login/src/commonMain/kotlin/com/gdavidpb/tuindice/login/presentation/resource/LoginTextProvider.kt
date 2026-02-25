package com.gdavidpb.tuindice.login.presentation.resource

interface LoginTextProvider {
	fun privacyPolicyTitle(): String
	fun termsAndConditionsTitle(): String
	fun invalidCredentials(): String
	fun userDisabled(): String
	fun serviceUnavailable(): String
	fun networkUnavailable(): String
	fun retry(): String
	fun timeout(): String
	fun passwordUpdated(): String
	fun invalidPassword(): String
	fun defaultError(): String
}
