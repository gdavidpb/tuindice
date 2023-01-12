package com.gdavidpb.tuindice.enrollmentproof.data.api

import com.gdavidpb.tuindice.enrollmentproof.data.api.responses.EnrollmentProofResponse
import retrofit2.Response
import retrofit2.http.*

interface EnrollmentProofApi {
	@GET("enrollmentProof")
	suspend fun enrollmentProof(): Response<EnrollmentProofResponse>
}