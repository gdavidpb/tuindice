package com.gdavidpb.tuindice.login.data.repository

interface MessagingApiDataSource {
	suspend fun subscribe(token: String)
}