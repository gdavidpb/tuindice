package com.gdavidpb.tuindice.login.domain.repository

interface MessagingRepository {
	suspend fun getToken(): String
}