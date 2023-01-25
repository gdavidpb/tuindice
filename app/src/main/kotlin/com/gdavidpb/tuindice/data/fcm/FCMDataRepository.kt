package com.gdavidpb.tuindice.data.fcm

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.data.fcm.source.LocalDataSource
import com.gdavidpb.tuindice.data.fcm.source.RemoteDataSource

class FCMDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource
) : MessagingRepository {
	override suspend fun getToken(): String? {
		return remoteDataSource.getToken()
	}

	override suspend fun subscribeToTopic(topic: String) {
		if (!localDataSource.isSubscribedToTopic(topic)) {
			remoteDataSource.subscribeToTopic(topic)
			localDataSource.saveSubscriptionTopic(topic)
		}
	}

	override suspend fun unsubscribeFromTopic(topic: String) {
		if (localDataSource.isSubscribedToTopic(topic))
			remoteDataSource.unsubscribeFromTopic(topic)
	}

	override suspend fun unsubscribeFromAllTopics() {
		localDataSource.getSubscribedTopics().forEach { topic ->
			remoteDataSource.unsubscribeFromTopic(topic)
		}
	}
}