package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.data.model.SyncResult

// Persists what a successful sync brought back: the academic record and the user it belongs to.
interface SyncResultLocalDataRepository {
	suspend fun saveSyncResult(result: SyncResult)
}
