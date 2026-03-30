package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface EnrollmentProofApiDataSource {
	suspend fun getEnrollmentProof(password: String): EnrollmentProof
}
