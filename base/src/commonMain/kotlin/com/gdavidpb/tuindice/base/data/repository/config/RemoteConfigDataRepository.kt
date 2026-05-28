package com.gdavidpb.tuindice.base.data.repository.config

interface RemoteConfigDataRepository {
	suspend fun fetch()
	fun getString(key: String): String?
}
