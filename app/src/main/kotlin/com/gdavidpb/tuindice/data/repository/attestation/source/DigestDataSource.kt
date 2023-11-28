package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationNonce
import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload
import com.gdavidpb.tuindice.base.utils.extension.encodeToBase64String
import com.gdavidpb.tuindice.data.repository.attestation.LocalDataSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class DigestDataSource : LocalDataSource {
	private val sha256 by lazy {
		MessageDigest.getInstance("SHA-256")
	}

	override suspend fun getNonce(identifier: String, payload: AttestationPayload): String {
		sha256.reset()

		val nonce = AttestationNonce(
			id = identifier,
			payload = payload
		)

		val json = Json.encodeToString(nonce)
		val data = json.toByteArray()
		val digest = sha256.digest(data)

		return digest.encodeToBase64String()
	}
}