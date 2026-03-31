package com.gdavidpb.tuindice.data.contract.messaging

interface MessagingLocalDataSource {
	suspend fun isSubscribed(): Boolean
	suspend fun getSubscribedToken(): String?
	suspend fun markAsSubscribed(token: String)
	suspend fun clearSubscription()
}
