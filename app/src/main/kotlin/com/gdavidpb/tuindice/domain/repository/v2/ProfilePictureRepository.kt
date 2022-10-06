package com.gdavidpb.tuindice.domain.repository.v2

import java.io.InputStream

interface ProfilePictureRepository {
	suspend fun getProfilePicture(): String
	suspend fun updateProfilePicture(stream: InputStream): String
	suspend fun deleteProfilePicture()
}