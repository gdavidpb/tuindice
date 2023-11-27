package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.data.repository.attestation.AttestationApi
import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource

class AttestationApiDataSource(
	private val attestationApi: AttestationApi
) : RemoteDataSource {
	override suspend fun getAttestationId(operation: String): String {
		return attestationApi
			.getAttestationId(operation)
			.getOrThrow()
			.id
	}
}