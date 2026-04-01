package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.model.EnrollmentProofResponse
import com.gdavidpb.tuindice.enrollmentproof.data.model.FetchEnrollmentProofRequest
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KtorEnrollmentProofApiDataSource(
	private val ktorClient: HttpClient
) : EnrollmentProofApiDataRepository {
	override suspend fun getEnrollmentProof(password: String): EnrollmentProof {
		val response = ktorClient.post("enrollment-proof/v1") {
			setBody(
				FetchEnrollmentProofRequest(password = password)
			)
		}.body<EnrollmentProofResponse>()

		return EnrollmentProof(
			source = response.name,
			content = response.content
		)
	}
}
