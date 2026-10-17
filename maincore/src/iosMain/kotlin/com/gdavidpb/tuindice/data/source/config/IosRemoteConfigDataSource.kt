package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class IosRemoteConfigDataSource(
	private val remoteConfigCapability: IosRemoteConfigCapability
) : RemoteConfigDataRepository {
	override suspend fun fetch() {
		remoteConfigCapability.fetchRemoteConfig()
	}

	override fun getString(key: String): String? {
		return remoteConfigCapability.remoteConfigString(key)
	}

	override fun observeUpdatedKeys(): Flow<Set<String>> = callbackFlow {
		val subscription = remoteConfigCapability.addRemoteConfigUpdateListener { keys ->
			trySend(keys)
		}

		awaitClose { subscription.cancel() }
	}
}
