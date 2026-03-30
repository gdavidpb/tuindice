package com.gdavidpb.tuindice.data.source.messaging

interface MessagingRemoteDataSource {
	suspend fun subscribe(messagingToken: String)
	suspend fun unsubscribe()
}
