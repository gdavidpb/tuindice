package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.data.repository.attestation.DigestDataSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import java.security.MessageDigest
import kotlin.io.encoding.Base64

class SHA256DigestDataSource(
	private val json: Json
) : DigestDataSource {
	override suspend fun digest(challenge: String, payload: AttestationPayload): String {
		val messageDigest = MessageDigest.getInstance("SHA-256")

		val jsonObject = buildJsonObject {
			put(
				key = "payload",
				element = json.encodeToJsonElement(payload)
			)

			put(
				key = "challenge",
				element = json.encodeToJsonElement(challenge)
			)
		}

		val jsonBytes = "$jsonObject".toByteArray()

		val hash = messageDigest.digest(jsonBytes)

		return Base64.UrlSafe.encode(hash)
	}
}