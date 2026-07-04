package com.gdavidpb.tuindice.base.data.repository.messaging


interface PushTokenDataRepository {
	suspend fun getToken(): String
}
