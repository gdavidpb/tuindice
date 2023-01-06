package com.gdavidpb.tuindice.record.data.enrollmentproof.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof

interface LocalDataSource {
	suspend fun getEnrollmentProof(uid: String, quarter: Quarter): EnrollmentProof
	suspend fun enrollmentProofExists(uid: String, quarter: Quarter): Boolean
	suspend fun saveEnrollmentProof(uid: String, quarter: Quarter, enrollmentProof: EnrollmentProof)
}