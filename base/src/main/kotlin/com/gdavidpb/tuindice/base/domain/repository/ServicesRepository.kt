package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.SignIn

interface ServicesRepository {
	/* Session */

	suspend fun signIn(basicToken: String, refreshToken: Boolean): SignIn

	/* Quarters */

	suspend fun getQuarters(): List<Quarter>

	/* Enrollment proof */

	suspend fun getEnrollmentProof(): EnrollmentProof
}