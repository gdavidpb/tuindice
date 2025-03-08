package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AttestationProviderMockDataSource : ProviderDataSource {
	override suspend fun getToken(nonce: String): String {
		return Uuid.random().toString()
	}
}