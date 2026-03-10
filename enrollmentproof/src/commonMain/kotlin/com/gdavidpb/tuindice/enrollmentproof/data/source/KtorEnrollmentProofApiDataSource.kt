package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.model.EnrollmentProofResponse
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorEnrollmentProofApiDataSource(
	private val ktorClient: HttpClient
) : EnrollmentProofApiDataSource {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		val response = ktorClient.get("enrollment-proof/v1")
			.body<EnrollmentProofResponse>()

		return EnrollmentProof(
			source = response.name,
			content = response.content
		)
	}
}
