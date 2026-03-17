package com.gdavidpb.tuindice.data.source.network

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.platform.IosDeviceCapability

class IosNetworkDataSource(
	private val deviceCapability: IosDeviceCapability
) : NetworkRepository {
	override fun isAvailable(): Boolean = deviceCapability.isNetworkAvailable()
}
