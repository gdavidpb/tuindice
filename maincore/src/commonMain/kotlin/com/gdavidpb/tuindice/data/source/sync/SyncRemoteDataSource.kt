package com.gdavidpb.tuindice.data.source.sync

interface SyncRemoteDataSource {
	suspend fun sync(password: String)
}
