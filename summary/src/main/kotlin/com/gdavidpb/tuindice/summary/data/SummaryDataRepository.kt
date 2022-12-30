package com.gdavidpb.tuindice.summary.data

import com.gdavidpb.tuindice.summary.data.source.api.APIDataSource
import com.gdavidpb.tuindice.summary.data.source.encoder.ImageEncoderDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.SummaryRepository

class SummaryDataRepository(
	private val apiDataSource: APIDataSource,
	private val imageEncoderDataSource: ImageEncoderDataSource
) : SummaryRepository {
	override suspend fun getProfilePicture(uid: String): ProfilePicture {
		return apiDataSource.getProfilePicture(uid)
	}

	override suspend fun uploadProfilePicture(path: String): ProfilePicture {
		val encodedPicture = imageEncoderDataSource.encodePicture(path)

		return apiDataSource.uploadProfilePicture(encodedPicture)
	}

	override suspend fun removeProfilePicture() {
		return apiDataSource.removeProfilePicture()
	}
}