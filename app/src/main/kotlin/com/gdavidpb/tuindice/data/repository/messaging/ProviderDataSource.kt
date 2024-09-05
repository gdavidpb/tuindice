package com.gdavidpb.tuindice.data.repository.messaging

interface ProviderDataSource {
	suspend fun getToken(): String?
}