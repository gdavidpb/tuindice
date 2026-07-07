package com.gdavidpb.tuindice.data.source.device

import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.platform.IosDeviceCapability
import platform.UIKit.UIDevice

class IosDeviceInfoDataSource(
	private val deviceCapability: IosDeviceCapability
) : DeviceInfoRepository {
	override fun appVersionName(): String = deviceCapability.appVersionName()

	override fun appVersionCode(): Long = deviceCapability.appVersionCode()

	override fun hasCamera(): Boolean = deviceCapability.hasCamera()

	override fun osDescription(): String {
		val device = UIDevice.currentDevice

		return "${device.systemName} ${device.systemVersion} · Apple ${device.model}"
	}
}
