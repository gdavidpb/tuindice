package com.gdavidpb.tuindice.about.domain.repository

class DefaultAboutVersionTextProvider : AboutVersionTextRepository {
	override fun environmentName(debug: Boolean): String {
		return if (debug) "Desarrollo" else "Producción"
	}

	override fun appVersion(
		environmentName: String,
		versionName: String,
		versionCode: Long
	): String {
		return "$environmentName v$versionName ($versionCode)"
	}
}
