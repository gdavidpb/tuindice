package com.gdavidpb.tuindice.summary.data.repository.user

interface DebugProfilePictureStorageDataRepository {
	suspend fun saveProfilePicture(content: ByteArray, remoteUrl: String): String
	suspend fun getLocalProfilePictureUrl(remoteUrl: String): String?
	suspend fun clearProfilePicture()
}
