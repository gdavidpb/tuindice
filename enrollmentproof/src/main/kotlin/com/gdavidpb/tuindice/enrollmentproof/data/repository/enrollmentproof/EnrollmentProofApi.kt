package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof

import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.response.EnrollmentProofResponse
import retrofit2.Response
import retrofit2.http.*

interface EnrollmentProofApi {
	@GET("enrollmentProof")
	suspend fun enrollmentProof(): Response<EnrollmentProofResponse>
}