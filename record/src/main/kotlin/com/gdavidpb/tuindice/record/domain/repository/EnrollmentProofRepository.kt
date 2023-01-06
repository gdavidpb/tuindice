package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter

interface EnrollmentProofRepository {
	suspend fun getEnrollmentProof(uid: String, quarter: Quarter): EnrollmentProof
}