package com.gdavidpb.tuindice.data.repository.messaging

interface LocalDataSource {
	suspend fun isSubscribed(): Boolean
	suspend fun getSubscribedToken(): String?
	suspend fun markAsSubscribed(token: String)
	suspend fun clearSubscription()
}
