package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.functions.requests.CheckCredentialsRequest
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class TuIndiceAPIMock(
	private val delegate: BehaviorDelegate<TuIndiceAPI>
) : TuIndiceAPI {
	override suspend fun checkCredentials(request: CheckCredentialsRequest): Response<Unit> {
		return delegate
			.returningResponse(Unit)
			.checkCredentials(request)
	}
}