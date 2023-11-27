package com.gdavidpb.tuindice.data.repository.messaging

interface RemoteDataSource {
	suspend fun enroll(messagingToken: String)
}