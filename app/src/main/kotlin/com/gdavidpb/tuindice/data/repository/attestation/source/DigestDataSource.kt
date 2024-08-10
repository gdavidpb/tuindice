package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationNonce
import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload
import com.gdavidpb.tuindice.base.utils.extension.encodeToBase64String
import com.gdavidpb.tuindice.data.repository.attestation.LocalDataSource
import com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation.SignInAttestationPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.security.MessageDigest

class DigestDataSource : LocalDataSource {
	private val nonceSerializer by lazy {
		Json {
			serializersModule = SerializersModule {
				polymorphic(AttestationPayload::class) {
					subclass(SignInAttestationPayload::class)
				}
			}
		}
	}

	override suspend fun getNonce(payload: AttestationPayload): String {
		val sha256 = MessageDigest.getInstance("SHA-256")

		val nonce = AttestationNonce(
			payload = payload
		)

		val json = nonceSerializer.encodeToString(nonce)
		val data = json.toByteArray()
		val digest = sha256.digest(data)

		return digest.encodeToBase64String()
	}
}