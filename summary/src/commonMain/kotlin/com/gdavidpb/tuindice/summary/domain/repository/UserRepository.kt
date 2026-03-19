package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
	suspend fun observeUserFlow(): Flow<User>
	suspend fun updateUser()

	suspend fun uploadProfilePicture(file: PlatformFile): ProfilePicture
	suspend fun removeProfilePicture()
}
