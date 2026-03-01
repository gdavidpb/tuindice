package com.gdavidpb.tuindice.data.repository.messaging

interface MessagingLocalDataSource {
	suspend fun isSubscribed(): Boolean
	suspend fun getSubscribedToken(): String?
	suspend fun markAsSubscribed(token: String)
	suspend fun clearSubscription()
}
