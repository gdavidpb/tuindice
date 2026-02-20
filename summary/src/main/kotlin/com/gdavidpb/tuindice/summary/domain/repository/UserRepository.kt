package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface UserRepository {
	suspend fun getUserFlow(): Flow<User>

	suspend fun uploadProfilePicture(inputStream: InputStream): ProfilePicture
	suspend fun removeProfilePicture()
}