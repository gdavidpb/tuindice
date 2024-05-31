package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.RemoteDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.mapper.toEnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.response.EnrollmentProofResponse
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class EnrollmentProofApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return ktorClient.get("enrollment-proof")
			.body<EnrollmentProofResponse>()
			.toEnrollmentProof()
	}
}