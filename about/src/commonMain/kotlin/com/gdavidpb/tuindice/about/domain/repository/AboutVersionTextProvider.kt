package com.gdavidpb.tuindice.about.domain.repository

interface AboutVersionTextProvider {
	fun environmentName(debug: Boolean): String

	fun appVersion(
		environmentName: String,
		versionName: String,
		versionCode: Long
	): String
}
