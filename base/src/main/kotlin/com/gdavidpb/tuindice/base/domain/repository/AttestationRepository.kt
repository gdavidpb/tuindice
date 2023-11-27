package com.gdavidpb.tuindice.base.domain.repository

interface AttestationRepository {
	suspend fun getToken(operation: String, payload: String): String
}