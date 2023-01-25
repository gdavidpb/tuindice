package com.gdavidpb.tuindice.enrollmentproof.data.api

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.RemoteDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.api.mappers.toEnrollmentProof

class ApiDataSource(
	private val enrollmentProofApi: EnrollmentProofApi
) : RemoteDataSource {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return enrollmentProofApi.enrollmentProof()
			.getOrThrow()
			.toEnrollmentProof()
	}
}