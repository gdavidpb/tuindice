package com.gdavidpb.tuindice.platform

interface IosDeviceCapability {
	fun appVersionName(): String
	fun appVersionCode(): Long
	fun hasCamera(): Boolean
	fun isNetworkAvailable(): Boolean
}
