package com.gdavidpb.tuindice.data.repository.messaging

interface RemoteDataSource {
	suspend fun subscribe(messagingToken: String)
	suspend fun unsubscribe()
}