package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository

class FakePendingChangesRepository(
	var pendingChanges: PendingChanges = PendingChanges.Empty,
	var flushResult: FlushPendingChangesResult = FlushPendingChangesResult.Success,
	var getPendingChangesThrowable: Throwable? = null
) : PendingChangesRepository {
	var getPendingChangesCalls = 0
		private set
	var flushCalls = 0
		private set

	override suspend fun getPendingChanges(): PendingChanges {
		getPendingChangesCalls++
		getPendingChangesThrowable?.let { throw it }
		return pendingChanges
	}

	override suspend fun flushPendingChanges(): FlushPendingChangesResult {
		flushCalls++
		return flushResult
	}
}
