package com.gdavidpb.tuindice.data.repository.attestation

interface LocalDataSource {
	suspend fun getNonce(payload: String): String
}