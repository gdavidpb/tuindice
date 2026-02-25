package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.IntegrityGateway
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository as LoginMessagingRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val iosDebugVariantModule: Module = module {
	factory<AttestationRepository> {
		IosDebugAttestationDataRepository(
			identityHttpClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}

	factory<IntegrityGateway> {
		get<AttestationRepository>()
	}

	factory<LoginMessagingRepository> {
		IosDebugLoginMessagingDataSource()
	}
}

internal fun iosVariantModules(buildVariant: IosBuildVariant): List<Module> {
	return when (buildVariant) {
		IosBuildVariant.DEBUG -> listOf(iosDebugVariantModule)
		IosBuildVariant.PRODUCTION -> emptyList()
	}
}

private class IosDebugAttestationDataRepository(
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

private class IosDebugLoginMessagingDataSource : LoginMessagingRepository {
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
