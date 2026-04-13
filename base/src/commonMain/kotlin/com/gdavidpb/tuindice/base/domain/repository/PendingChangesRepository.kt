package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges

interface PendingChangesRepository {
	suspend fun getPendingChanges(): PendingChanges
	suspend fun flushPendingChanges(): FlushPendingChangesResult
}
