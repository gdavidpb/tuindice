package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.domain.model.SignIn

interface ApiRepository {
	suspend fun signIn(basicToken: String, refreshToken: Boolean): SignIn
	@Deprecated("This will be removed.")
	suspend fun sync()
	suspend fun getEnrollmentProof(): EnrollmentProof
}