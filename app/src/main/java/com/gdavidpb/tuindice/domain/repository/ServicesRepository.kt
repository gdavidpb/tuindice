package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.domain.model.SignIn

interface ServicesRepository {
	suspend fun signIn(basicToken: String, refreshToken: Boolean): SignIn
	suspend fun sync()
	suspend fun getEnrollmentProof(): EnrollmentProof
}