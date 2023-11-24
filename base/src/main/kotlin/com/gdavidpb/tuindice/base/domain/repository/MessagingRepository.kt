package com.gdavidpb.tuindice.base.domain.repository

interface MessagingRepository {
	suspend fun enroll()
	suspend fun subscribeToTopic(topic: String)
	suspend fun unsubscribeFromTopic(topic: String)
	suspend fun unsubscribeFromAllTopics()
}