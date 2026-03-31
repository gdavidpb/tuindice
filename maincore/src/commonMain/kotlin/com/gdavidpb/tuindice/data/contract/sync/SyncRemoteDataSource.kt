package com.gdavidpb.tuindice.data.contract.sync

interface SyncRemoteDataSource {
	suspend fun sync(password: String)
}
