package com.gdavidpb.tuindice.base.domain.repository

interface DeviceInfoGateway {
	fun appVersionName(): String
	fun appVersionCode(): Long
	fun hasCamera(): Boolean
}
