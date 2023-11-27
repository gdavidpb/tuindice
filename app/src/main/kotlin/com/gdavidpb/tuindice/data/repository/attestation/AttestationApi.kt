package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.data.repository.attestation.source.api.response.AttestationIdResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AttestationApi {
	@GET("attestation")
	suspend fun getAttestationId(
		@Query("operation") operation: String
	): Response<AttestationIdResponse>
}