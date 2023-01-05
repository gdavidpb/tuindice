package com.gdavidpb.tuindice.summary.data.api

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.summary.data.api.mappers.toAccount
import com.gdavidpb.tuindice.summary.data.api.mappers.toProfilePicture
import com.gdavidpb.tuindice.summary.data.account.source.RemoteDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

class ApiDataSource(
	private val summaryApi: SummaryApi
) : RemoteDataSource {
	override suspend fun getAccount(): Account {
		return summaryApi.getAccount()
			.getOrThrow()
			.toAccount()
	}

	override suspend fun uploadProfilePicture(encodedPicture: String): ProfilePicture {
		return summaryApi.uploadProfilePicture(encodedPicture)
			.getOrThrow()
			.toProfilePicture()
	}

	override suspend fun removeProfilePicture() {
		summaryApi.deleteProfilePicture()
			.getOrThrow()
	}
}