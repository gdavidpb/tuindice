package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

class AndroidRemoteConfigDataSource(
	private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigDataSource {
	override suspend fun fetch() {
		remoteConfig.fetchAndActivate().await()
	}

	override fun getString(key: String): String? {
		return remoteConfig.getString(key).takeIf { value ->
			value.isNotBlank()
		}
	}
}
