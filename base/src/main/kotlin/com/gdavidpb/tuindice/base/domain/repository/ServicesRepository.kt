package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.SignIn

interface ServicesRepository {
	suspend fun signIn(basicToken: String, refreshToken: Boolean): SignIn
	suspend fun sync()
	suspend fun getEnrollmentProof(): EnrollmentProof
}