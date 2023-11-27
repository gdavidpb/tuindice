package com.gdavidpb.tuindice.data.repository.attestation

interface LocalDataSource {
	suspend fun getNonce(identifier: String, payload: String): String
}