package com.gdavidpb.tuindice.base.data.contract.config

interface RemoteConfigDataSource {
	suspend fun fetch()
	fun getString(key: String): String?
}
