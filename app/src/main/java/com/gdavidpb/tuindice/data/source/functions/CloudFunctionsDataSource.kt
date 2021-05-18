package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.data.source.functions.requests.CheckCredentialsRequest
import com.gdavidpb.tuindice.data.source.functions.requests.SignInRequest
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.functions.SignInResult
import com.gdavidpb.tuindice.domain.repository.FunctionsRepository
import com.gdavidpb.tuindice.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.utils.mappers.toSignInResult

class CloudFunctionsDataSource(
	private val tuIndiceAPI: TuIndiceAPI
) : FunctionsRepository {
	override suspend fun signIn(credentials: Credentials): SignInResult {
		val request = SignInRequest(
			usbId = credentials.usbId,
			password = credentials.password
		)

		return tuIndiceAPI.signIn(request)
			.getOrThrow()
			.toSignInResult()
	}

	override suspend fun checkCredentials(credentials: Credentials) {
		val request = CheckCredentialsRequest(
			usbId = credentials.usbId,
			password = credentials.password
		)

		tuIndiceAPI.checkCredentials(request)
			.getOrThrow()
	}
}