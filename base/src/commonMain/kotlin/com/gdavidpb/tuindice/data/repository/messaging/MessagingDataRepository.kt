package com.gdavidpb.tuindice.data.repository.messaging

import com.gdavidpb.tuindice.data.source.messaging.MessagingLocalDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingRemoteDataSource
import com.gdavidpb.tuindice.data.source.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class MessagingDataRepository(
	private val localDataSource: MessagingLocalDataSource,
	private val remoteDataSource: MessagingRemoteDataSource,
	private val pushTokenDataSource: PushTokenDataSource
) : MessagingRepository {
	override suspend fun subscribe() {
		val messagingToken = pushTokenDataSource.getToken()
			.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable.")

		val isSubscribed = localDataSource.isSubscribed()
		val subscribedToken = localDataSource.getSubscribedToken()
		val hasMatchingToken = isSubscribed && subscribedToken == messagingToken

		if (hasMatchingToken) return

		remoteDataSource.subscribe(messagingToken)
		localDataSource.markAsSubscribed(messagingToken)
	}

	override suspend fun unsubscribe() {
		runCatching { remoteDataSource.unsubscribe() }
		localDataSource.clearSubscription()
	}
}
