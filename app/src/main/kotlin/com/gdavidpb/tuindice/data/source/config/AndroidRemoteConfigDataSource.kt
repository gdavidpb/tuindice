package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

class AndroidRemoteConfigDataSource(
	private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigDataRepository {
	override suspend fun fetch() {
		remoteConfig.fetchAndActivate().await()
	}

	override fun getString(key: String): String? {
		return remoteConfig.getString(key).takeIf { value ->
			value.isNotBlank()
		}
	}
}
