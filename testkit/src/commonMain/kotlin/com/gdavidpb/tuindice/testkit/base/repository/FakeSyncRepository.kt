package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSyncRepository : SyncRepository {
	val scheduledSyncCalls = mutableListOf<String>()
	val scheduledSyncPolicies = mutableListOf<SyncPolicy>()

	override fun scheduleSync(password: String, policy: SyncPolicy) {
		scheduledSyncCalls += password
		scheduledSyncPolicies += policy
	}

	override fun observeSyncInProgress(): Flow<Boolean> = flowOf(false)
}
