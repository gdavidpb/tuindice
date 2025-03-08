package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.login.domain.repository.SignInRepository

class SignInDataRepository(
	private val remoteDataSource: RemoteDataSource
) : SignInRepository {
	override suspend fun auth(
		username: String,
		password: String,
		attestation: Attestation
	): String {
		return remoteDataSource.auth(username, password, attestation)
	}
}