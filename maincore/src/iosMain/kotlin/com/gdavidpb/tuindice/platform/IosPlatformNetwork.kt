package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.di.buildStructuredUserAgent
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

fun createIosUserAgent(deviceCapability: IosDeviceCapability): String {
	val appVersionName = deviceCapability.appVersionName().ifBlank { "0.0.0" }
	val appVersionCode = deviceCapability.appVersionCode().coerceAtLeast(0L)
	val device = UIDevice.currentDevice
	val osVersion = device.systemVersion.ifBlank { "Unknown" }
	val osCode = osVersion
		.substringBefore(".")
		.toIntOrNull()
		?: 0
	val osId = NSBundle.mainBundle.objectForInfoDictionaryKey("DTPlatformBuild")
		?.toString()
		?.takeIf { it.isNotBlank() }
		?: "Unknown"
	val model = device.model.takeIf { it.isNotBlank() } ?: "Unknown"

	return buildStructuredUserAgent(
		appVersionName = appVersionName,
		appVersionCode = appVersionCode,
		osName = "iOS",
		osVersion = osVersion,
		osCode = osCode,
		osId = osId,
		manufacturer = "Apple",
		model = model
	)
}
