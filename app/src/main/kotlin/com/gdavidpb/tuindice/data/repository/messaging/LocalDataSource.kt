package com.gdavidpb.tuindice.data.repository.messaging

interface LocalDataSource {
	suspend fun isSubscribed(): Boolean
	suspend fun markAsSubscribed()
}