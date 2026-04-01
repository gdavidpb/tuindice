package com.gdavidpb.tuindice.data.repository.sync

interface SyncRemoteDataRepository {
	suspend fun sync(password: String)
}
