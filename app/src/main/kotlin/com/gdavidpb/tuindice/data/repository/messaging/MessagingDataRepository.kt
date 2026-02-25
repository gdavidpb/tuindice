package com.gdavidpb.tuindice.data.repository.messaging

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class MessagingDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val providerDataSource: ProviderDataSource
) : MessagingRepository {
	override suspend fun subscribe() {
		val messagingToken = providerDataSource.getToken()
			?.takeIf { token -> token.isNotBlank() }
			?: return

		val isSubscribed = localDataSource.isSubscribed()
		val subscribedToken = localDataSource.getSubscribedToken()
		val hasMatchingToken = isSubscribed && subscribedToken == messagingToken

		if (hasMatchingToken) {
			return
		}

		remoteDataSource.subscribe(messagingToken)
		localDataSource.markAsSubscribed(token = messagingToken)
	}

	override suspend fun unsubscribe() {
		runCatching {
			remoteDataSource.unsubscribe()
		}
		localDataSource.clearSubscription()
	}
}
