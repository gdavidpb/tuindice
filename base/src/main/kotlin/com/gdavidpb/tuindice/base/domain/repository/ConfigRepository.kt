package com.gdavidpb.tuindice.base.domain.repository

interface ConfigRepository {
    suspend fun tryFetchAndActivate()
    fun getString(key: String): String
    fun getBoolean(key: String): Boolean
    fun getDouble(key: String): Double
    fun getLong(key: String): Long
    fun getStringList(key: String): List<String>
}