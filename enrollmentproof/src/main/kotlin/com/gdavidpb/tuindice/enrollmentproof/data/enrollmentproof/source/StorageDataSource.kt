package com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface StorageDataSource {
	suspend fun getEnrollmentProof(uid: String, quarter: Quarter): EnrollmentProof
	suspend fun enrollmentProofExists(uid: String, quarter: Quarter): Boolean
	suspend fun saveEnrollmentProof(uid: String, quarter: Quarter, enrollmentProof: EnrollmentProof)
}