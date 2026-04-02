package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class UserDataSource(
	private val localDataSource: LocalDataRepository,
	private val remoteDataSource: RemoteDataRepository,
	private val settingsDataSource: SettingsDataRepository,
	private val profilePictureInputDataSource: ProfilePictureInputDataRepository,
	private val pictureEncoderDataSource: PictureEncoderDataRepository
) : UserRepository {
	override suspend fun observeUserFlow(): Flow<User> {
		return localDataSource.getUserFlow()
			.mapNotNull { localUser -> localUser }
			.distinctUntilChanged()
	}

	override suspend fun updateUser() {
		if (settingsDataSource.isGetUserOnCooldown()) return

		val remoteUser = remoteDataSource.getUser()

		localDataSource.updateUser(user = remoteUser)
		settingsDataSource.setGetUserOnCooldown()
	}

	override suspend fun uploadProfilePicture(file: PlatformFile): ProfilePicture {
		val normalizedFile = profilePictureInputDataSource.normalizeInput(file = file)

		try {
			val encodedImage = pictureEncoderDataSource.encodePicture(file = normalizedFile)

			return remoteDataSource.uploadProfilePicture(
				content = encodedImage.content,
				mimeType = encodedImage.mimeType
			).also { profilePicture ->
				updateLocalProfilePicture(url = profilePicture.url)
			}
		} finally {
			if (normalizedFile.path != file.path && normalizedFile.exists()) {
				normalizedFile.delete(mustExist = false)
			}
		}
	}

	override suspend fun removeProfilePicture() {
		runCatching { remoteDataSource.removeProfilePicture() }
			.onFailure { throwable ->
				if (!throwable.isNotFound())
					throw throwable
			}

		updateLocalProfilePicture(url = "")
	}

	private suspend fun updateLocalProfilePicture(url: String) {
		val currentUser = checkNotNull(localDataSource.getUserFlow().first()) {
			"Expected a local user before updating the profile picture."
		}

		localDataSource.updateUser(user = currentUser.copy(pictureUrl = url))
	}
}
