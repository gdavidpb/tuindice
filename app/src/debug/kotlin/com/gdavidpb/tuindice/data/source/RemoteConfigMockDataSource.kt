package com.gdavidpb.tuindice.data.source

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import java.io.IOException

open class RemoteConfigMockDataSource(
        private val remoteConfig: FirebaseRemoteConfig,
        private val googleJson: Gson
) : ConfigRepository {
    override suspend fun tryFetchAndActivate() {
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