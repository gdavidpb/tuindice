package com.gdavidpb.tuindice.enrollmentproof.domain.repository

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface EnrollmentProofRepository {
	suspend fun getEnrollmentProof(uid: String): EnrollmentProof?
}