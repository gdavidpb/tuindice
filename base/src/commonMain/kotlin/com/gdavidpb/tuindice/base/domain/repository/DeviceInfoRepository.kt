package com.gdavidpb.tuindice.base.domain.repository

interface DeviceInfoRepository {
	fun appVersionName(): String
	fun appVersionCode(): Long
	fun hasCamera(): Boolean
	fun osDescription(): String
}
