package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter

interface TuIndiceRepository {
	suspend fun getEnrollmentProof(quarter: Quarter): EnrollmentProof

	suspend fun getQuarters(uid: String): List<Quarter>
}