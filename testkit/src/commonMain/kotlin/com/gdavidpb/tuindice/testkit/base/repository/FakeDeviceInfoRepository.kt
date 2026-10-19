package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository

class FakeDeviceInfoRepository(
	private val versionName: String = "1.0.0",
	private val versionCode: Long = 1L,
	private val deviceHasCamera: Boolean = false,
	private val deviceOsDescription: String = "Android 14 · Test Device"
) : DeviceInfoRepository {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode

	override fun hasCamera(): Boolean = deviceHasCamera

	override fun osDescription(): String = deviceOsDescription
}
