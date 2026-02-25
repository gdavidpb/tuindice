package com.gdavidpb.tuindice.ui.resource

data class HostUiTexts(
	val googleServicesUnavailableTitle: String,
	val googleServicesUnavailableMessage: String,
	val googleServicesUnavailableExit: String,
	val externalResourceTitle: String,
	val externalResourceMessage: String,
	val externalResourceOpen: String,
	val externalResourceCancel: String
)

interface HostUiTextProvider {
	fun getValues(): HostUiTexts
}
