package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.data.model.SyncResult

interface SyncRemoteDataRepository {
	suspend fun sync(password: String): SyncResult
}
