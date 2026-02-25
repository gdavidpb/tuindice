package com.gdavidpb.tuindice.about.data.repository

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutVersionTextRepository
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository

class AboutDataRepository(
	private val versionTextProvider: AboutVersionTextRepository,
	private val deviceInfoGateway: DeviceInfoRepository,
	private val appEnvironmentRepository: AppEnvironmentRepository
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
