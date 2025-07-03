package com.gdavidpb.tuindice.login.data.repository

interface MessagingDataSource {
	suspend fun getToken(): String
}