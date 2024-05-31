package com.gdavidpb.tuindice.data.repository.attestation.source

import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.api.response.AttestationIdResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class AttestationApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun getAttestationId(operation: String): String {
		return ktorClient.get("attestation") {
			parameter("operation", operation)
		}
			.body<AttestationIdResponse>()
			.id
	}
}