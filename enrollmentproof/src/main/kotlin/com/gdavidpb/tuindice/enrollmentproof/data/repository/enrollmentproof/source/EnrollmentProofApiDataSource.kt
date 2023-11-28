package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.RemoteDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.mapper.toEnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.EnrollmentProofApi

class EnrollmentProofApiDataSource(
	private val enrollmentProofApi: EnrollmentProofApi
) : RemoteDataSource {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return enrollmentProofApi.enrollmentProof()
			.getOrThrow()
			.toEnrollmentProof()
	}
}