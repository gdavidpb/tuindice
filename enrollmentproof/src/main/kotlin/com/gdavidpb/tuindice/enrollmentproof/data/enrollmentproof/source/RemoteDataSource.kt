package com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface RemoteDataSource {
	suspend fun getEnrollmentProof(): EnrollmentProof
}