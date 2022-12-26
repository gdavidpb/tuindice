package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.SignIn
import com.gdavidpb.tuindice.base.domain.repository.ServicesRepository
import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.utils.mappers.toEnrollmentProof
import com.gdavidpb.tuindice.utils.mappers.toSignIn

class CloudFunctionsDataSource(
	private val tuIndiceAPI: TuIndiceAPI
) : ServicesRepository {
	override suspend fun signIn(basicToken: String, refreshToken: Boolean): SignIn {
		return tuIndiceAPI.signIn(basicToken = basicToken, refreshToken = refreshToken)
			.getOrThrow()
			.toSignIn()
	}

	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return tuIndiceAPI.enrollmentProof()
			.getOrThrow()
			.toEnrollmentProof()
	}
}