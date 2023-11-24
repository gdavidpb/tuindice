package com.gdavidpb.tuindice.data.messaging.source

interface RemoteDataSource {
	suspend fun enroll(messagingToken: String)
}