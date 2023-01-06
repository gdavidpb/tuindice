package com.gdavidpb.tuindice.record.data.enrollmentproof.source

import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof

interface RemoteDataSource {
	suspend fun getEnrollmentProof(): EnrollmentProof
}