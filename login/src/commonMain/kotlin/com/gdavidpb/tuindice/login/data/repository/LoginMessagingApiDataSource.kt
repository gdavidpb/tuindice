package com.gdavidpb.tuindice.login.data.repository

interface LoginMessagingApiDataSource {
	suspend fun subscribe(token: String)
}
