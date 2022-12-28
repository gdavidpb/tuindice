package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.SignIn
import com.gdavidpb.tuindice.base.domain.repository.ServicesRepository
import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.data.source.functions.mappers.toEnrollmentProof
import com.gdavidpb.tuindice.data.source.functions.mappers.toQuarter
import com.gdavidpb.tuindice.data.source.functions.mappers.toSignIn
import com.gdavidpb.tuindice.login.mapping.encodeCredentialsBasic

class CloudFunctionsDataSource(
	private val tuIndiceAPI: TuIndiceAPI
) : ServicesRepository {
	override suspend fun signIn(
		username: String,
		password: String,
		messagingToken: String?,
		refreshToken: Boolean
	): SignIn {
		return tuIndiceAPI.signIn(
			basicToken = encodeCredentialsBasic(username, password),
			refreshToken = refreshToken,
			messagingToken = messagingToken
		)
			.getOrThrow()
			.toSignIn()
	}

	override suspend fun getQuarters(): List<Quarter> {
		return tuIndiceAPI.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toQuarter() }
	}

	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return tuIndiceAPI.enrollmentProof()
			.getOrThrow()
			.toEnrollmentProof()
	}
}