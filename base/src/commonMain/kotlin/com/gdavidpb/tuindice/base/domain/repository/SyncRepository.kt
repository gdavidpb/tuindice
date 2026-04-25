package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncRepository {
	fun scheduleSync(password: String)
	fun observeSyncInProgress(): Flow<Boolean>
}
