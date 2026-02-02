package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.data.repository.attestation.DigestDataSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.security.MessageDigest
import kotlin.io.encoding.Base64

class SHA256DigestDataSource : DigestDataSource {
	override suspend fun digest(challenge: String, payload: String): String {
		val messageDigest = MessageDigest.getInstance("SHA-256")
		val json = Json.parseToJsonElement(payload).jsonObject + ("challenge" to challenge)
		val data = "$json".toByteArray()

		val hash = messageDigest.digest(data)

		return Base64.encode(hash)
	}
}