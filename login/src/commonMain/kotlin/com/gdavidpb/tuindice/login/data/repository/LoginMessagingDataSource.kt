package com.gdavidpb.tuindice.login.data.repository

interface LoginMessagingDataSource {
	suspend fun getToken(): String
}
