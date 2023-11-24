package com.gdavidpb.tuindice.data.messaging

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.data.messaging.source.LocalDataSource
import com.gdavidpb.tuindice.data.messaging.source.ProviderDataSource
import com.gdavidpb.tuindice.data.messaging.source.RemoteDataSource

class MessagingDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val providerDataSource: ProviderDataSource
) : MessagingRepository {
	override suspend fun enroll() {
		if (localDataSource.isEnrolled()) return

		val messagingToken = providerDataSource.getToken()

		if (messagingToken != null) {
			remoteDataSource.enroll(messagingToken)
			localDataSource.markAsEnrolled()
		}
	}

	override suspend fun subscribeToTopic(topic: String) {
		if (!localDataSource.isSubscribedToTopic(topic)) {
			providerDataSource.subscribeToTopic(topic)
			localDataSource.saveSubscriptionTopic(topic)
		}
	}

	override suspend fun unsubscribeFromTopic(topic: String) {
		if (localDataSource.isSubscribedToTopic(topic))
			providerDataSource.unsubscribeFromTopic(topic)
	}

	override suspend fun unsubscribeFromAllTopics() {
		localDataSource.getSubscribedTopics().forEach { topic ->
			providerDataSource.unsubscribeFromTopic(topic)
		}
	}
}