package com.gdavidpb.tuindice.about.data.repository

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository

class AboutDataRepository(
	private val environmentDataSource: EnvironmentDataSource,
	private val deviceInfoDataSource: AppInfoDataSource,
) : AboutRepository {
	override suspend fun getVersionDescription(): String {
		val isDebug = environmentDataSource.isDebugEnvironment()
		val environmentName = if (isDebug) "Desarrollo" else "Producción"
		val versionName = deviceInfoDataSource.appVersionName()
		val versionCode = deviceInfoDataSource.appVersionCode()

		return "$environmentName v$versionName ($versionCode)"
	}
}
