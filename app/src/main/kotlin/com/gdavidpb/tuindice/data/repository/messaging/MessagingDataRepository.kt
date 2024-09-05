package com.gdavidpb.tuindice.data.repository.messaging

import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository

class MessagingDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val providerDataSource: ProviderDataSource
) : MessagingRepository {
	override suspend fun subscribe() {
		if (localDataSource.isSubscribed()) return

		val messagingToken = providerDataSource.getToken()

		if (messagingToken != null) {
			remoteDataSource.subscribe(messagingToken)
			localDataSource.markAsSubscribed()
		}
	}

	override suspend fun unsubscribe() {
		remoteDataSource.unsubscribe()
	}
}