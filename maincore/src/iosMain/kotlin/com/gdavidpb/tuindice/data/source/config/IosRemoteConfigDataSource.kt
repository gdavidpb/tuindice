package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.data.contract.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability

class IosRemoteConfigDataSource(
	private val remoteConfigCapability: IosRemoteConfigCapability
) : RemoteConfigDataSource {
	override suspend fun fetch() {
		remoteConfigCapability.fetchRemoteConfig()
	}

	override fun getString(key: String): String? {
		return remoteConfigCapability.remoteConfigString(key)
	}
}
