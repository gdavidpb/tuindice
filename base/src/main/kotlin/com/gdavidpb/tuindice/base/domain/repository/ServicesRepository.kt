package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter

interface ServicesRepository {
	/* Quarters */

	suspend fun getQuarters(): List<Quarter>

	/* Enrollment proof */

	suspend fun getEnrollmentProof(): EnrollmentProof
}