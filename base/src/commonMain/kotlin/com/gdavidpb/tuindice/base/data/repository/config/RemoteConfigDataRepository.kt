package com.gdavidpb.tuindice.base.data.repository.config

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface RemoteConfigDataRepository {
	suspend fun fetch()
	fun getString(key: String): String?

	// Keys whose value changed while the app runs. A source that cannot observe updates emits nothing.
	fun observeUpdatedKeys(): Flow<Set<String>> = emptyFlow()
}
