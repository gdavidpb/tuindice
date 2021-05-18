package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.functions.requests.CheckCredentialsRequest
import com.gdavidpb.tuindice.data.source.functions.requests.SignInRequest
import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import com.gdavidpb.tuindice.services.responses.defaultSignInResponse
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class TuIndiceAPIMock(
	private val delegate: BehaviorDelegate<TuIndiceAPI>
) : TuIndiceAPI {
	override suspend fun signIn(request: SignInRequest): Response<SignInResponse> {
		return delegate
			.returningResponse(defaultSignInResponse)
			.signIn(request)
	}

	override suspend fun checkCredentials(request: CheckCredentialsRequest): Response<Unit> {
		return delegate
			.returningResponse(Unit)
			.checkCredentials(request)
	}
}