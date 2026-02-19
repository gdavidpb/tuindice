package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.data.repository.attestation.DigestDataSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import java.security.MessageDigest
import kotlin.io.encoding.Base64

class SHA256DigestDataSource(
	private val jsonFormat: Json
) : DigestDataSource {
	override suspend fun digest(challenge: String, payload: AttestationPayload): String {
		val messageDigest = MessageDigest.getInstance("SHA-256")
		val json = jsonFormat.encodeToJsonElement(payload).jsonObject + ("challenge" to challenge)
		val jsonBytes = "$json".toByteArray()

		val hash = messageDigest.digest(jsonBytes)

		return Base64.UrlSafe.encode(hash)
	}
}