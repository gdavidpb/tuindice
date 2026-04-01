package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class UserDataSource(
	private val localDataSource: LocalDataRepository,
	private val remoteDataSource: RemoteDataRepository,
	private val settingsDataSource: SettingsDataRepository,
	private val pictureEncoderDataSource: PictureEncoderDataRepository
) : UserRepository {
	private companion object {
		const val MAX_PROFILE_PICTURE_UPLOAD_BYTES = 1_048_576
	}

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
		val encodedImage = pictureEncoderDataSource.encodePicture(file = file)

		if (encodedImage.content.size > MAX_PROFILE_PICTURE_UPLOAD_BYTES) {
			throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.SizeExceeded)
		}

		return remoteDataSource.uploadProfilePicture(
			content = encodedImage.content,
			mimeType = encodedImage.mimeType
		).also { profilePicture ->
			updateLocalProfilePicture(url = profilePicture.url)
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
