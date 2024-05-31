package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource
import java.util.UUID

class AttestationProviderMockDataSource : ProviderDataSource {
	override suspend fun getToken(nonce: String): String {
		return UUID.randomUUID().toString()
	}
}