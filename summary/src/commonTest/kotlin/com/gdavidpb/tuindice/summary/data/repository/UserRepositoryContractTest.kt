package com.gdavidpb.tuindice.summary.data.repository

import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.data.repository.user.UserDataRepository
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeLocalDataSource
import com.gdavidpb.tuindice.summary.testing.FakePictureEncoderDataSource
import com.gdavidpb.tuindice.summary.testing.FakeRemoteDataSource
import com.gdavidpb.tuindice.summary.testing.FakeSettingsDataSource
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserRepositoryContractTest {
	@Test
	fun getUserFlow_emitsLocalThenRemote_andMarksCooldown() = runTest {
		val staleUser = DEFAULT_SUMMARY_USER.copy(
			pictureUrl = "https://cdn.tuindice.app/profile/stale.jpg"
		)
		val localDataSource = FakeLocalDataSource(initialUser = staleUser)
		val remoteDataSource = FakeRemoteDataSource(user = DEFAULT_SUMMARY_USER)
		val settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		val repository = UserDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		val emissions = repository.getUserFlow()
			.take(2)
			.toList()

		assertEquals(listOf(staleUser, DEFAULT_SUMMARY_USER), emissions)
		assertEquals(listOf(DEFAULT_SUMMARY_USER), localDataSource.savedUsers)
		assertEquals(1, remoteDataSource.getUserCalls)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun uploadProfilePicture_encodesUri_savesLocalUrl_andReturnsRemotePicture() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val encoderDataSource = FakePictureEncoderDataSource()
		val repository = UserDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			pictureEncoderDataSource = encoderDataSource
		)
		val uri = PlatformUri("content://profile/new.jpg")

		val picture = repository.uploadProfilePicture(uri)

		assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE, picture)
		assertEquals(uri, encoderDataSource.lastUri)
		assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE.url, localDataSource.savedProfilePictureUrls.single())
		assertEquals("image/jpeg", remoteDataSource.uploadCalls.single().second)
	}

	@Test
	fun removeProfilePicture_clearsLocalPicture_andDelegatesRemoteRemoval() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource()
		val repository = UserDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		repository.removeProfilePicture()

		assertEquals(listOf(""), localDataSource.savedProfilePictureUrls)
		assertEquals(1, remoteDataSource.removeCalls)
	}
}
