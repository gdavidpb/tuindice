package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.record.data.api.mappers.toEnrollmentProof
import com.gdavidpb.tuindice.record.data.enrollmentproof.source.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof

class EnrollmentProofApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return recordApi.enrollmentProof()
			.getOrThrow()
			.toEnrollmentProof()
	}
}