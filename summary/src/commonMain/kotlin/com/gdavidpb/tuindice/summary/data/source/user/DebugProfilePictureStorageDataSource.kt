package com.gdavidpb.tuindice.summary.data.source.user

interface DebugProfilePictureStorageDataSource {
	suspend fun saveProfilePicture(content: ByteArray, remoteUrl: String): String
	suspend fun getLocalProfilePictureUrl(remoteUrl: String): String?
	suspend fun clearProfilePicture()
}
