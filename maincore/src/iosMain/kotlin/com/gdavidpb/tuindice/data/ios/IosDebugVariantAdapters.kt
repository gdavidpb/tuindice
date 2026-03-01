package com.gdavidpb.tuindice.data.ios

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.login.data.repository.LoginMessagingDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class IosDebugAttestationDataRepository(
	private val identityHttpClient: HttpClient
) : AttestationRepository {
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		val challengeId = runCatching {
			identityHttpClient
				.get("auth/challenge")
				.body<IosDebugChallengeResponse>()
				.id
		}.getOrDefault(IOS_DEBUG_ATTESTATION_ID_FALLBACK)

		return Attestation(
			id = challengeId,
			token = IOS_DEBUG_ATTESTATION_TOKEN,
			provider = AttestationProvider.APP_ATTEST,
			keyId = IOS_DEBUG_ATTESTATION_KEY_ID
		)
	}
}

internal class IosDebugLoginMessagingDataSource : LoginMessagingDataSource {
	override suspend fun getToken(): String {
		return IOS_DEBUG_PUSH_TOKEN
	}
}

@Serializable
private data class IosDebugChallengeResponse(
	@SerialName("id") val id: String
)

private const val IOS_DEBUG_ATTESTATION_ID_FALLBACK = "ios-debug-attestation-id"
private const val IOS_DEBUG_ATTESTATION_TOKEN = "ios-debug-attestation-token"
private const val IOS_DEBUG_ATTESTATION_KEY_ID = "ios-debug-attestation-key-id"
private const val IOS_DEBUG_PUSH_TOKEN = "ios-debug-push-token"
