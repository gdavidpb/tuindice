package com.gdavidpb.tuindice.summary.data.repository

import com.gdavidpb.tuindice.summary.data.source.UserDataSource
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeLocalDataSource
import com.gdavidpb.tuindice.summary.testing.FakePictureEncoderDataSource
import com.gdavidpb.tuindice.summary.testing.FakeProfilePictureInputDataSource
import com.gdavidpb.tuindice.summary.testing.FakeRemoteDataSource
import com.gdavidpb.tuindice.summary.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.write
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UserRepositoryContractTest {
	@Test
	fun observeUserFlow_emitsLocalUser_withoutRefreshing() = runTest {
		val staleUser = DEFAULT_SUMMARY_USER.copy(
			pictureUrl = "https://cdn.tuindice.app/profile/stale.jpg"
		)
		val localDataSource = FakeLocalDataSource(initialUser = staleUser)
		val remoteDataSource = FakeRemoteDataSource(user = DEFAULT_SUMMARY_USER)
		val settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		val emission = repository.observeUserFlow().first()

		assertEquals(staleUser, emission)
		assertTrue(localDataSource.savedUsers.isEmpty())
		assertEquals(0, remoteDataSource.getUserCalls)
		assertFalse(settingsDataSource.cooldownMarked)
	}

	@Test
	fun updateUser_savesRemoteUser_andMarksCooldown_whenNotOnCooldown() = runTest {
		val localDataSource = FakeLocalDataSource(initialUser = null)
		val remoteDataSource = FakeRemoteDataSource(user = DEFAULT_SUMMARY_USER)
		val settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		repository.updateUser()

		assertEquals(listOf(DEFAULT_SUMMARY_USER), localDataSource.savedUsers)
		assertEquals(1, remoteDataSource.getUserCalls)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun updateUser_doesNothing_whenCooldownIsActive() = runTest {
		val localDataSource = FakeLocalDataSource(initialUser = DEFAULT_SUMMARY_USER)
		val remoteDataSource = FakeRemoteDataSource()
		val settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		repository.updateUser()

		assertTrue(localDataSource.savedUsers.isEmpty())
		assertEquals(0, remoteDataSource.getUserCalls)
		assertFalse(settingsDataSource.cooldownMarked)
	}

	@Test
	fun uploadProfilePicture_encodesFile_savesLocalUrl_andReturnsRemotePicture() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val encoderDataSource = FakePictureEncoderDataSource()
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = encoderDataSource
		)
		val file = PlatformFile("content://profile/new.jpg")

		val picture = repository.uploadProfilePicture(file)

		assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE, picture)
		assertEquals(file, encoderDataSource.lastFile)
		assertEquals(
			DEFAULT_SUMMARY_PROFILE_PICTURE.url,
			localDataSource.savedUsers.single().pictureUrl
		)
		assertEquals("image/jpeg", remoteDataSource.uploadCalls.single().second)
	}

	@Test
	fun uploadProfilePicture_normalizesInputBeforeEncoding_andRemovesTemporaryFile() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val encoderDataSource = FakePictureEncoderDataSource()
		val originalFile = PlatformFile("content://profile/new.heic")
		val normalizedFile = createTempFile(
			prefix = "normalized",
			extension = "jpg",
			content = byteArrayOf(1, 2, 3)
		)
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(normalizedFile = normalizedFile),
			pictureEncoderDataSource = encoderDataSource
		)

		try {
			repository.uploadProfilePicture(originalFile)

			assertEquals(normalizedFile, encoderDataSource.lastFile)
			assertEquals(false, normalizedFile.exists())
		} finally {
			deleteFileIfExists(normalizedFile)
		}
	}

	@Test
	fun uploadProfilePicture_rejects_encoded_images_that_exceed_the_upload_limit() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val encoderDataSource = FakePictureEncoderDataSource(throwable = IllegalArgumentException())
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = encoderDataSource
		)

		assertFailsWith<IllegalArgumentException> {
			repository.uploadProfilePicture(PlatformFile("content://profile/too-large.jpg"))
		}

		assertTrue(remoteDataSource.uploadCalls.isEmpty())
		assertTrue(localDataSource.savedUsers.isEmpty())
	}

	@Test
	fun removeProfilePicture_clearsLocalPicture_andDelegatesRemoteRemoval() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource()
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		repository.removeProfilePicture()

		assertEquals("", localDataSource.savedUsers.single().pictureUrl)
		assertEquals(1, remoteDataSource.removeCalls)
	}

	@Test
	fun removeProfilePicture_clearsLocalPicture_whenRemoteReturnsNotFound() = runTest {
		val localDataSource = FakeLocalDataSource()
		val remoteDataSource = FakeRemoteDataSource(
			removeThrowable = clientRequestException(
				statusCode = HttpStatusCode.NotFound,
				path = "/users/v1/picture"
			)
		)
		val repository = UserDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			profilePictureInputDataSource = FakeProfilePictureInputDataSource(),
			pictureEncoderDataSource = FakePictureEncoderDataSource()
		)

		repository.removeProfilePicture()

		assertEquals("", localDataSource.savedUsers.single().pictureUrl)
		assertEquals(1, remoteDataSource.removeCalls)
	}

	private suspend fun createTempFile(
		prefix: String,
		extension: String,
		content: ByteArray
	) = (FileKit.filesDir / "summaryRepositoryTests" / "${prefix}_${Random.nextInt(1_000_000)}.$extension").also { file ->
		(FileKit.filesDir / "summaryRepositoryTests").createDirectories()
		file.write(content)
	}

	private suspend fun deleteFileIfExists(file: PlatformFile) {
		if (file.exists()) {
			file.delete(mustExist = false)
		}
	}
}
