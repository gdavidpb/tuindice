package com.gdavidpb.tuindice.data.source.messaging

interface PushTokenDataSource {
	suspend fun getToken(): String
}
