package com.gdavidpb.tuindice.data.source.device

import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.platform.IosDeviceCapability

class IosDeviceInfoDataSource(
	private val deviceCapability: IosDeviceCapability
) : DeviceInfoRepository {
	override fun appVersionName(): String = deviceCapability.appVersionName()

	override fun appVersionCode(): Long = deviceCapability.appVersionCode()

	override fun hasCamera(): Boolean = deviceCapability.hasCamera()
}
