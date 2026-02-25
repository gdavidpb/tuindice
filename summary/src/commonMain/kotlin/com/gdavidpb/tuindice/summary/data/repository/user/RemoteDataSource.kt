package com.gdavidpb.tuindice.summary.data.repository.user

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

interface RemoteDataSource {
	suspend fun getUser(): User

	suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture
	suspend fun removeProfilePicture()
}
