package com.gdavidpb.tuindice.enrollmentproof.data.repository


import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

interface StorageDataRepository {
	suspend fun getEnrollmentProof(name: String): EnrollmentProof
	suspend fun enrollmentProofExists(name: String): Boolean
	suspend fun saveEnrollmentProof(name: String, enrollmentProof: EnrollmentProof)
}