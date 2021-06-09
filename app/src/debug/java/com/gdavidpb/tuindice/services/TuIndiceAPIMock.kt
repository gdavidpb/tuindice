package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import com.gdavidpb.tuindice.services.responses.defaultSignInResponse
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class TuIndiceAPIMock(
	private val delegate: BehaviorDelegate<TuIndiceAPI>
) : TuIndiceAPI {
	override suspend fun signIn(
		basicToken: String,
		refreshToken: Boolean
	): Response<SignInResponse> {
		return delegate
			.returningResponse(defaultSignInResponse)
			.signIn(basicToken, refreshToken)
	}
}