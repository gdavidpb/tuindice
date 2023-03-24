package com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface StorageDataSource {
	suspend fun getEnrollmentProof(uid: String, name: String): EnrollmentProof
	suspend fun enrollmentProofExists(uid: String, name: String): Boolean
	suspend fun saveEnrollmentProof(uid: String, name: String, enrollmentProof: EnrollmentProof)
}