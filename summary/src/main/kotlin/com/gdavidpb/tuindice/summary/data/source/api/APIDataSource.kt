package com.gdavidpb.tuindice.summary.data.source.api

import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.summary.data.source.ServiceDataSource
import com.gdavidpb.tuindice.summary.data.source.api.mappers.toProfile
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.data.source.api.mappers.toProfilePicture
import com.gdavidpb.tuindice.summary.domain.model.Profile

class APIDataSource(
	private val summaryAPI: SummaryAPI
) : ServiceDataSource {
	override suspend fun getProfile(): Profile {
		return summaryAPI.getProfile()
			.getOrThrow()
			.toProfile()
	}

	override suspend fun getProfilePicture(uid: String): ProfilePicture {
		return summaryAPI.getProfilePicture(uid)
			.getOrThrow()
			.toProfilePicture()
	}

	override suspend fun uploadProfilePicture(encodedPicture: String): ProfilePicture {
		return summaryAPI.uploadProfilePicture(encodedPicture)
			.getOrThrow()
			.toProfilePicture()
	}

	override suspend fun removeProfilePicture() {
		summaryAPI.deleteProfilePicture()
			.getOrThrow()
	}
}