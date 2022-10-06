package com.gdavidpb.tuindice.base.domain.repository

interface MessagingRepository {
	suspend fun getToken(): String?
	suspend fun subscribeToTopic(topic: String)
	suspend fun unsubscribeFromTopic(topic: String)
}