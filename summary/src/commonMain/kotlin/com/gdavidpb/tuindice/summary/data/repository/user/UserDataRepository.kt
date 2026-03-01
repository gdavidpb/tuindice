package com.gdavidpb.tuindice.summary.data.repository.user

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class UserDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource,
	private val pictureEncoderDataSource: PictureEncoderDataSource
) : UserRepository {
	override suspend fun getUserFlow(): Flow<User> {
		return localDataSource.getUserFlow()
			.distinctUntilChanged()
			.transform { localUser ->
				val isOnCooldown = settingsDataSource.isGetUserOnCooldown()

				if (localUser != null)
					emit(localUser)

				if (!isOnCooldown) {
					val remoteUser = remoteDataSource.getUser()

					localDataSource.saveUser(user = remoteUser)

					settingsDataSource.setGetUserOnCooldown()

					emit(remoteUser)
				}
			}
	}

	override suspend fun uploadProfilePicture(uri: PlatformUri): ProfilePicture {
		val encodedImage = pictureEncoderDataSource.encodePicture(uri = uri)

		return remoteDataSource.uploadProfilePicture(
			content = encodedImage.content,
			mimeType = encodedImage.mimeType
		).also { profilePicture ->
			localDataSource.saveProfilePicture(url = profilePicture.url)
		}
	}

	override suspend fun removeProfilePicture() {
		remoteDataSource.removeProfilePicture()
		localDataSource.saveProfilePicture(url = "")
	}
}
