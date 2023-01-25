package com.gdavidpb.tuindice.data.source.fcm.source

interface RemoteDataSource {
	suspend fun getToken(): String?
	suspend fun subscribeToTopic(topic: String)
	suspend fun unsubscribeFromTopic(topic: String)
}