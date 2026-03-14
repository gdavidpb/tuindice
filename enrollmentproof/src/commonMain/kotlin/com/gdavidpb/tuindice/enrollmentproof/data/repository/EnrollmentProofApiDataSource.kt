package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface EnrollmentProofApiDataSource {
	suspend fun getEnrollmentProof(password: String): EnrollmentProof
}
