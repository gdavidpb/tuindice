package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.data.repository.user.DebugProfilePictureStorageDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

class DebugSummaryRemoteDataSource(
	private val apiRemoteDataSource: RemoteDataRepository,
	private val debugProfilePictureStorageDataSource: DebugProfilePictureStorageDataRepository
) : RemoteDataRepository {
	override suspend fun getUser(): User {
		val remoteUser = apiRemoteDataSource.getUser()

		if (remoteUser.pictureUrl.isBlank()) {
			debugProfilePictureStorageDataSource.clearProfilePicture()
			return remoteUser
		}

		val localPictureUrl = debugProfilePictureStorageDataSource.getLocalProfilePictureUrl(
			remoteUrl = remoteUser.pictureUrl
		)

		return if (localPictureUrl != null)
			remoteUser.copy(pictureUrl = localPictureUrl)
		else
			remoteUser
	}

	override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
		val remoteProfilePicture = apiRemoteDataSource.uploadProfilePicture(
			content = content,
			mimeType = mimeType
		)
		val localPictureUrl = debugProfilePictureStorageDataSource.saveProfilePicture(
			content = content,
			remoteUrl = remoteProfilePicture.url
		)

		return ProfilePicture(url = localPictureUrl)
	}

	override suspend fun removeProfilePicture() {
		apiRemoteDataSource.removeProfilePicture()
		debugProfilePictureStorageDataSource.clearProfilePicture()
	}
}
