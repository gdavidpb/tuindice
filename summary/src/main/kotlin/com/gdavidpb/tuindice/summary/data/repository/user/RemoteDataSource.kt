package com.gdavidpb.tuindice.summary.data.repository.user

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import java.io.InputStream

interface RemoteDataSource {
	suspend fun getUser(): User

	suspend fun uploadProfilePicture(inputStream: InputStream): ProfilePicture
	suspend fun removeProfilePicture()
}