package com.gdavidpb.tuindice.base.data.repository

interface SecureKeyValueDataRepository {
	suspend fun getString(key: String): String?
	suspend fun putString(key: String, value: String)
	suspend fun remove(key: String)
	suspend fun clear()
}
