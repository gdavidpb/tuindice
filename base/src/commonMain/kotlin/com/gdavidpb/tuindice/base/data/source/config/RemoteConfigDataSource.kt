package com.gdavidpb.tuindice.base.data.source.config

interface RemoteConfigDataSource {
	suspend fun fetch()
	fun getString(key: String): String?
}
