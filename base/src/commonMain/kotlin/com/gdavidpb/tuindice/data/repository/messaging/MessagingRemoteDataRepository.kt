package com.gdavidpb.tuindice.data.repository.messaging


interface MessagingRemoteDataRepository {
	suspend fun subscribe(messagingToken: String)
	suspend fun unsubscribe()
}
