package com.gdavidpb.tuindice.data.repository.messaging

interface PushTokenDataSource {
	suspend fun getToken(): String
}
