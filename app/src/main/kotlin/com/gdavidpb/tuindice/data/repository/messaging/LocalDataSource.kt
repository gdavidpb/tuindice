package com.gdavidpb.tuindice.data.repository.messaging

interface LocalDataSource {
	suspend fun isEnrolled(): Boolean
	suspend fun markAsEnrolled()

	suspend fun getSubscribedTopics(): List<String>
	suspend fun saveSubscriptionTopic(topic: String)
	suspend fun isSubscribedToTopic(topic: String): Boolean
}