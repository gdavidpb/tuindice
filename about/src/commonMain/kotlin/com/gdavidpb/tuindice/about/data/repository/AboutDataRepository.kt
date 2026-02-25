package com.gdavidpb.tuindice.about.data.repository

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutVersionTextProvider
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoGateway

class AboutDataRepository(
	private val versionTextProvider: AboutVersionTextProvider,
	private val deviceInfoGateway: DeviceInfoGateway,
	private val appEnvironmentRepository: AppEnvironmentGateway
) : AboutRepository {
	override suspend fun getVersionDescription(): String {
		val environmentName = versionTextProvider.environmentName(
			debug = appEnvironmentRepository.getEnvironment().debug
		)

		return versionTextProvider.appVersion(
			environmentName = environmentName,
			versionName = deviceInfoGateway.appVersionName(),
			versionCode = deviceInfoGateway.appVersionCode()
		)
	}
}
