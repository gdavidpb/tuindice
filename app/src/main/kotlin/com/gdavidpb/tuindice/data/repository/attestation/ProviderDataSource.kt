package com.gdavidpb.tuindice.data.repository.attestation

interface ProviderDataSource {
	suspend fun getToken(nonce: String): String
}