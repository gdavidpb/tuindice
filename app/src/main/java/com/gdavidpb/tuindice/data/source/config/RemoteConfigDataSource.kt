package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.domain.repository.ConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import okio.IOException

open class RemoteConfigDataSource(
        private val remoteConfig: FirebaseRemoteConfig,
        private val googleJson: Gson
) : ConfigRepository {
    override suspend fun tryFetchAndActivate() = runCatching { remoteConfig.fetchAndActivate().await() }.getOrNull() == true

    override fun getString(key: String) = remoteConfig.getString(key)

    override fun getBoolean(key: String) = remoteConfig.getBoolean(key)

    override fun getDouble(key: String) = remoteConfig.getDouble(key)

    override fun getLong(key: String) = remoteConfig.getLong(key)

    override fun getStringList(key: String): List<String> {
        val json = remoteConfig.getString(key)

        return runCatching {
            googleJson.fromJson(json, Array<String>::class.java).toList()
        }.getOrElse { throwable ->
            throw IOException("Could not read '$key'. ${throwable.message}", throwable)
        }
    }
}