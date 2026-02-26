package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository

class DefaultAppInfoDataSource(
	private val deviceInfoRepository: DeviceInfoRepository
) : AppInfoDataSource {
	override fun appVersionName(): String {
		return deviceInfoRepository.appVersionName()
	}

	override fun appVersionCode(): Long {
		return deviceInfoRepository.appVersionCode()
	}
}
