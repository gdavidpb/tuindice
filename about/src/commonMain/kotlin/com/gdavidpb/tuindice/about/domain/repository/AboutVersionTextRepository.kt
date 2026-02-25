package com.gdavidpb.tuindice.about.domain.repository

interface AboutVersionTextRepository {
	fun environmentName(debug: Boolean): String

	fun appVersion(
		environmentName: String,
		versionName: String,
		versionCode: Long
	): String
}
