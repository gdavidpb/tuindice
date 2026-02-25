package com.gdavidpb.tuindice.login.domain.repository

interface MessagingApiRepository {
	suspend fun subscribe(token: String)
}