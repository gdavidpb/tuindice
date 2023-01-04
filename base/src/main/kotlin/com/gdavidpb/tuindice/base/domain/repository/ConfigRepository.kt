package com.gdavidpb.tuindice.base.domain.repository

interface ConfigRepository {
	suspend fun tryFetch()

	@Deprecated("This will be removed.")
	fun getString(key: String): String

	@Deprecated("This will be removed.")

	fun getBoolean(key: String): Boolean

	@Deprecated("This will be removed.")
	fun getDouble(key: String): Double

	@Deprecated("This will be removed.")
	fun getLong(key: String): Long

	@Deprecated("This will be removed.")
	fun getStringList(key: String): List<String>
}