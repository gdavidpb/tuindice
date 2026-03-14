package com.gdavidpb.tuindice.data.repository.sync

interface SyncRemoteDataSource {
	suspend fun sync(password: String)
}
