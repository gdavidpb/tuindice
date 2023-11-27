package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.utils.extension.encodeToBase64String
import com.gdavidpb.tuindice.data.repository.attestation.LocalDataSource
import java.security.MessageDigest

class DigestDataSource : LocalDataSource {
	private val sha256 by lazy {
		MessageDigest.getInstance("SHA-256")
	}

	override suspend fun getNonce(identifier: String, payload: String): String {
		val data = identifier.toByteArray() + payload.toByteArray()

		sha256.reset()

		return sha256.digest(data).encodeToBase64String()
	}
}