package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.logging.appLogger
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AndroidRemoteConfigDataSource(
	private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigDataRepository {
	private val logger = appLogger(tag = "RemoteConfig")

	override suspend fun fetch() {
		remoteConfig.fetchAndActivate().await()
	}

	override fun getString(key: String): String? {
		return remoteConfig.getString(key).takeIf { value ->
			value.isNotBlank()
		}
	}

	override fun observeUpdatedKeys(): Flow<Set<String>> = callbackFlow {
		val registration = remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
			override fun onUpdate(configUpdate: ConfigUpdate) {
				// The fetched values reach getString() only once they are activated.
				remoteConfig.activate().addOnCompleteListener {
					trySend(configUpdate.updatedKeys)
				}
			}

			override fun onError(error: FirebaseRemoteConfigException) {
				logger.e(error) { "Real-time Remote Config update failed" }
			}
		})

		awaitClose { registration.remove() }
	}
}
