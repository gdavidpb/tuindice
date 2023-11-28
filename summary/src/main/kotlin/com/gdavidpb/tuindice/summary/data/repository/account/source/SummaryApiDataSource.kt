package com.gdavidpb.tuindice.summary.data.repository.account.source

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.summary.data.repository.account.SummaryApi
import com.gdavidpb.tuindice.summary.data.repository.account.source.api.mapper.toAccount
import com.gdavidpb.tuindice.summary.data.repository.account.source.api.mapper.toProfilePicture
import com.gdavidpb.tuindice.summary.data.repository.account.RemoteDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

class SummaryApiDataSource(
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