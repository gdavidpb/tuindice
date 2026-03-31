package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.contract.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.contract.EnvironmentDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository

class AboutDataSource(
	private val environmentDataSource: EnvironmentDataSource,
	private val appInfoDataSource: AppInfoDataSource,
) : AboutRepository {
	override suspend fun getVersionDescription(): String {
		val isDebug = environmentDataSource.isDebugEnvironment()
		val environmentName = if (isDebug) "Desarrollo" else "Producción"
		val versionName = appInfoDataSource.appVersionName()
		val versionCode = appInfoDataSource.appVersionCode()

		return "$environmentName v$versionName ($versionCode)"
	}
}
