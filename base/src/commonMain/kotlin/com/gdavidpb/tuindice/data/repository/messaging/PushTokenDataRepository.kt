package com.gdavidpb.tuindice.data.repository.messaging


interface PushTokenDataRepository {
	suspend fun getToken(): String
}
