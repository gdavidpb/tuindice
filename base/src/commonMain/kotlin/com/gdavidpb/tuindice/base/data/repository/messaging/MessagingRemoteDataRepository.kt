package com.gdavidpb.tuindice.base.data.repository.messaging


interface MessagingRemoteDataRepository {
	suspend fun subscribe(messagingToken: String)
	suspend fun unsubscribe()
}
