package com.gdavidpb.tuindice.data.repository.attestation

interface DigestDataSource {
	suspend fun digest(challenge: String, payload: String): String
}