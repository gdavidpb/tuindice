package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability

class IosRemoteConfigDataSource(
	private val remoteConfigCapability: IosRemoteConfigCapability
) : RemoteConfigDataRepository {
	override suspend fun fetch() {
		remoteConfigCapability.fetchRemoteConfig()
	}

	override fun getString(key: String): String? {
		return remoteConfigCapability.remoteConfigString(key)
	}
}
