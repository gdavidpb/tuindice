package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.data.contract.messaging.MessagingLocalDataSource
import com.gdavidpb.tuindice.data.contract.messaging.MessagingRemoteDataSource
import com.gdavidpb.tuindice.data.contract.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository

class MessagingDataSource(
	private val localDataSource: MessagingLocalDataSource,
	private val remoteDataSource: MessagingRemoteDataSource,
	private val pushTokenDataSource: PushTokenDataSource
) : MessagingDataRepository {
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
