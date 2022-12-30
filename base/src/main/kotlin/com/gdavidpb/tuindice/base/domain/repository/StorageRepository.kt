package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter

interface StorageRepository {
	suspend fun getEnrollmentProof(quarter: Quarter): EnrollmentProof
	suspend fun existsEnrollmentProof(quarter: Quarter): Boolean
	suspend fun saveEnrollmentProof(quarter: Quarter, enrollmentProof: EnrollmentProof)

	suspend fun clear()
}