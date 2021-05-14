package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.data.source.functions.requests.CheckCredentialsRequest
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.FunctionsRepository
import com.gdavidpb.tuindice.utils.extensions.getOrThrow

class CloudFunctionsDataSource(
	private val tuIndiceAPI: TuIndiceAPI
) : FunctionsRepository {
	override suspend fun checkCredentials(credentials: Credentials) {
		val request = CheckCredentialsRequest(
			usbId = credentials.usbId,
			password = credentials.password
		)

		tuIndiceAPI.checkCredentials(request).getOrThrow()
	}
}