package com.gdavidpb.tuindice.data.contract.messaging


interface PushTokenDataSource {
	suspend fun getToken(): String
}
