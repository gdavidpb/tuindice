package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.domain.model.SignIn
import com.gdavidpb.tuindice.domain.repository.ApiRepository
import com.gdavidpb.tuindice.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.utils.mappers.toSignIn

class CloudFunctionsDataSource(
	private val tuIndiceAPI: TuIndiceAPI
) : ApiRepository {
	override suspend fun signIn(basicToken: String, refreshToken: Boolean): SignIn {
		return tuIndiceAPI.signIn(basicToken = basicToken, refreshToken = refreshToken)
			.getOrThrow()
			.toSignIn()
	}

	override suspend fun sync() {
		return tuIndiceAPI.sync()
			.getOrThrow()
	}
}