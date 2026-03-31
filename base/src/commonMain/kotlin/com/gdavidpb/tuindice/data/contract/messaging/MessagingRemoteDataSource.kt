package com.gdavidpb.tuindice.data.contract.messaging


interface MessagingRemoteDataSource {
	suspend fun subscribe(messagingToken: String)
	suspend fun unsubscribe()
}
