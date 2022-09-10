package com.gdavidpb.tuindice.domain.repository.v2

import com.gdavidpb.tuindice.domain.model.EnrollmentProof

interface EnrollmentProofRepository {
	suspend fun getEnrollmentProof(): EnrollmentProof
}