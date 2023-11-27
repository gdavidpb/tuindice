package com.gdavidpb.tuindice.data.repository.messaging

interface ProviderDataSource {
	suspend fun getToken(): String?
	suspend fun subscribeToTopic(topic: String)
	suspend fun unsubscribeFromTopic(topic: String)
}