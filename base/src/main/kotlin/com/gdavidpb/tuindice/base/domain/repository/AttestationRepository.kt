package com.gdavidpb.tuindice.base.domain.repository

interface AttestationRepository {
	suspend fun getToken(payload: String): String
}