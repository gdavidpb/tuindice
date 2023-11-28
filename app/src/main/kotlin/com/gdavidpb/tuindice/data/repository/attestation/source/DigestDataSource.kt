package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.utils.extension.encodeToBase64String
import com.gdavidpb.tuindice.data.repository.attestation.LocalDataSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.MessageDigest

class DigestDataSource : LocalDataSource {
	private val sha256 by lazy {
		MessageDigest.getInstance("SHA-256")
	}

	override suspend fun getNonce(identifier: String, payload: String): String {
		sha256.reset()

		val payloadJson = Json.decodeFromString<JsonObject>(payload)
		val nonceJson = buildJsonObject {
			put("id", identifier)
			put("payload", payloadJson)
		}

		val json = Json.encodeToString(nonceJson)
		val data = json.toByteArray()
		val digest = sha256.digest(data)

		return digest.encodeToBase64String()
	}
}