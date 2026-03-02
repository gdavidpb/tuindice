package com.gdavidpb.tuindice.data.ios

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.di.IosAttestationCapability
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal class IosAttestationDataRepository(
	private val httpClientProvider: () -> HttpClient,
	private val json: Json,
	private val attestationCapability: IosAttestationCapability
) : AttestationRepository {
	@OptIn(ExperimentalEncodingApi::class)
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		val httpClient = httpClientProvider()
		val challenge = httpClient
			.get("auth/challenge")
			.body<ChallengeResponse>()

		val noncePayload = buildJsonObject {
			put("payload", json.encodeToJsonElement(payload))
			put("challenge", json.encodeToJsonElement(challenge.challenge))
		}.toString()

		val attestationInput = Base64.UrlSafe.encode(noncePayload.encodeToByteArray())

		val providerAttestation = attestationCapability.requestAttestation(attestationInput)
			?: throw IllegalStateException("Attestation token unavailable on iOS bridge.")
		check(providerAttestation.provider == AttestationProvider.APP_ATTEST) {
			"Unsupported iOS attestation provider: ${providerAttestation.provider}."
		}
		val keyId = providerAttestation.keyId
			?.takeIf { value -> value.isNotBlank() }
			?: throw IllegalStateException(
				"Attestation keyId unavailable for iOS APP_ATTEST provider."
			)

		return Attestation(
			id = challenge.id,
			token = providerAttestation.token,
			provider = providerAttestation.provider,
			keyId = keyId
		)
	}
}

@Serializable
private data class ChallengeResponse(
	@SerialName("id") val id: String,
	@SerialName("challenge") val challenge: String
)
