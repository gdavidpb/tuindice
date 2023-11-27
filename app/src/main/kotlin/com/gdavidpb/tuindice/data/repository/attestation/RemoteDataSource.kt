package com.gdavidpb.tuindice.data.repository.attestation

interface RemoteDataSource {
	suspend fun getAttestationId(operation: String): String
}