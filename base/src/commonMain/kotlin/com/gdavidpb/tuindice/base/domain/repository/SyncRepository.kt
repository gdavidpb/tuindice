package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
	fun scheduleSync(password: String, policy: SyncPolicy = SyncPolicy.RespectCooldown)
	fun observeSyncInProgress(): Flow<Boolean>
}
