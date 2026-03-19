package com.gdavidpb.tuindice.summary.data.repository.user.source

import com.gdavidpb.tuindice.summary.testing.FakeLocalDataSource
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugSummaryUserRepositoryContractTest {
	@Test
	fun updateUser_seedsDebugUser_whenLocalStoreIsEmpty() = runTest {
		val localDataSource = FakeLocalDataSource(initialUser = null)
		val repository = DebugSummaryUserRepository(
			localDataSource = localDataSource,
			sourceName = "test-summary-debug",
			loadDelayMillis = 0L,
			pictureOperationDelayMillis = 0L
		)

		repository.updateUser()

		val user = repository.observeUserFlow().first()

		assertEquals(DEFAULT_DEBUG_SUMMARY_USER.fullName, user.fullName)
		assertEquals(DEFAULT_DEBUG_SUMMARY_USER.email, user.email)
		assertEquals(DEFAULT_DEBUG_SUMMARY_USER.fullName, localDataSource.savedUsers.single().fullName)
	}

	@Test
	fun uploadProfilePicture_savesSelectedLocalFilePath() = runTest {
		val localDataSource = FakeLocalDataSource(initialUser = null)
		val repository = DebugSummaryUserRepository(
			localDataSource = localDataSource,
			sourceName = "test-summary-debug",
			loadDelayMillis = 0L,
			pictureOperationDelayMillis = 0L
		)
		val file = PlatformFile("content://profile/debug.jpg")

		val picture = repository.uploadProfilePicture(file)

		assertEquals(file.path, picture.url)
		assertEquals(file.path, localDataSource.savedUsers.last().pictureUrl)
	}

	@Test
	fun removeProfilePicture_clearsSavedLocalPicture() = runTest {
		val localDataSource = FakeLocalDataSource(initialUser = null)
		val repository = DebugSummaryUserRepository(
			localDataSource = localDataSource,
			sourceName = "test-summary-debug",
			loadDelayMillis = 0L,
			pictureOperationDelayMillis = 0L
		)

		repository.removeProfilePicture()

		assertEquals("", localDataSource.savedUsers.last().pictureUrl)
	}
}
