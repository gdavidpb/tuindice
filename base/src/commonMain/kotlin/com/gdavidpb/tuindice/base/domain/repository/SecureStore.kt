package com.gdavidpb.tuindice.base.domain.repository

interface SecureStore {
	fun contains(key: String): Boolean
	fun getString(key: String): String?
	fun putString(key: String, value: String)
	fun clear()
}
