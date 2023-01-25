package com.gdavidpb.tuindice.data.fcm.source

interface LocalDataSource {
	suspend fun getSubscribedTopics(): List<String>
	suspend fun saveSubscriptionTopic(topic: String)
	suspend fun isSubscribedToTopic(topic: String): Boolean
}