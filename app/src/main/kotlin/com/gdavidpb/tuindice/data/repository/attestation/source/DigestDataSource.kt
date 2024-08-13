package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.data.repository.attestation.LocalDataSource
import io.ktor.util.encodeBase64
import java.security.MessageDigest

class DigestDataSource : LocalDataSource {
	override suspend fun getNonce(payload: String): String {
		val sha256 = MessageDigest.getInstance("SHA-256")

		val data = payload.toByteArray()
		val digest = sha256.digest(data)

		return digest.encodeBase64()
	}
}