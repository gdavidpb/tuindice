package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import kotlinx.coroutines.flow.Flow

interface UserRepository {
	suspend fun getUserFlow(): Flow<User>

	suspend fun uploadProfilePicture(uri: PlatformUri): ProfilePicture
	suspend fun removeProfilePicture()
}
