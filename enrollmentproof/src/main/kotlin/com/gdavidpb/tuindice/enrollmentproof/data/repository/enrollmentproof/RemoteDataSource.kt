package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface RemoteDataSource {
	suspend fun getEnrollmentProof(): EnrollmentProof
}