package com.gdavidpb.tuindice.data.repository.messaging

interface MessagingRemoteDataSource {
	suspend fun subscribe(messagingToken: String)
	suspend fun unsubscribe()
}
