package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.data.repository.messaging.MessagingLocalDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.MessagingRemoteDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class MessagingDataSource(
	private val localDataSource: MessagingLocalDataRepository,
	private val remoteDataSource: MessagingRemoteDataRepository,
	private val pushTokenDataSource: PushTokenDataRepository
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
