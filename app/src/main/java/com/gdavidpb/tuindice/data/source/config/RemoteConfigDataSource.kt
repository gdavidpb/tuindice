package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.utils.extensions.awaitOrNull
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import okio.IOException

open class RemoteConfigDataSource(
        private val remoteConfig: FirebaseRemoteConfig,
        private val googleJson: Gson
) : ConfigRepository {
    override suspend fun tryFetchAndActivate() {
        remoteConfig.fetchAndActivate().awaitOrNull()
    }

    override fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    override fun getDouble(key: String): Double {
        return remoteConfig.getDouble(key)
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    override fun getStringList(key: String): List<String> {
        val json = remoteConfig.getString(key)

        return runCatching {
            googleJson.fromJson(json, Array<String>::class.java).toList()
        }.getOrElse { throwable ->
            throw IOException("Could not read '$key'. ${throwable.message}", throwable)
        }
    }
}