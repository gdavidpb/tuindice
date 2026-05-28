package com.gdavidpb.tuindice.enrollmentproof.data.repository


import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface EnrollmentProofApiDataRepository {
	suspend fun getEnrollmentProof(password: String): EnrollmentProof
}
