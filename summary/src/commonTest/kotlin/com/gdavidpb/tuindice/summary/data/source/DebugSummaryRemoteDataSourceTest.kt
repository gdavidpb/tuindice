package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.data.contract.user.DebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.contract.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DebugSummaryRemoteDataSourceTest {
	@Test
	fun uploadProfilePicture_saves_local_override_and_returns_local_path() = runTest {
		val remoteDataSource = RecordingRemoteDataSource(
			profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE
		)
		val storageDataSource = FakeDebugProfilePictureStorageDataSource()
		val dataSource = DebugSummaryRemoteDataSource(
			apiRemoteDataSource = remoteDataSource,
			debugProfilePictureStorageDataSource = storageDataSource
		)
		val content = byteArrayOf(1, 2, 3, 4)

		val result = dataSource.uploadProfilePicture(
			content = content,
			mimeType = "image/png"
		)

		assertEquals("file:///debug/profile-picture.img", result.url)
		assertEquals(1, remoteDataSource.uploadCalls.size)
		assertContentEquals(content, remoteDataSource.uploadCalls.single().first)
		assertEquals("image/png", remoteDataSource.uploadCalls.single().second)
		assertContentEquals(content, storageDataSource.savedContent)
		assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE.url, storageDataSource.savedRemoteUrl)
	}

	@Test
	fun getUser_prefers_local_override_when_remote_picture_matches_saved_remote_url() = runTest {
		val remotePictureUrl = "https://cdn.tuindice.app/profile/uploaded.jpg"
		val remoteDataSource = RecordingRemoteDataSource(
			user = DEFAULT_SUMMARY_USER.copy(pictureUrl = remotePictureUrl)
		)
		val storageDataSource = FakeDebugProfilePictureStorageDataSource(
			localPictureUrl = "file:///debug/profile-picture.img",
			storedRemoteUrl = remotePictureUrl
		)
		val dataSource = DebugSummaryRemoteDataSource(
			apiRemoteDataSource = remoteDataSource,
			debugProfilePictureStorageDataSource = storageDataSource
		)

		val result = dataSource.getUser()

		assertEquals("file:///debug/profile-picture.img", result.pictureUrl)
		assertEquals(0, storageDataSource.clearCalls)
	}

	@Test
	fun getUser_clears_local_override_when_remote_picture_is_empty() = runTest {
		val remoteDataSource = RecordingRemoteDataSource(
			user = DEFAULT_SUMMARY_USER.copy(pictureUrl = "")
		)
		val storageDataSource = FakeDebugProfilePictureStorageDataSource(
			localPictureUrl = "file:///debug/profile-picture.img",
			storedRemoteUrl = "https://cdn.tuindice.app/profile/uploaded.jpg"
		)
		val dataSource = DebugSummaryRemoteDataSource(
			apiRemoteDataSource = remoteDataSource,
			debugProfilePictureStorageDataSource = storageDataSource
		)

		val result = dataSource.getUser()

		assertEquals("", result.pictureUrl)
		assertEquals(1, storageDataSource.clearCalls)
	}

	@Test
	fun removeProfilePicture_delegates_remote_delete_and_clears_local_override() = runTest {
		val remoteDataSource = RecordingRemoteDataSource()
		val storageDataSource = FakeDebugProfilePictureStorageDataSource(
			localPictureUrl = "file:///debug/profile-picture.img",
			storedRemoteUrl = DEFAULT_SUMMARY_PROFILE_PICTURE.url
		)
		val dataSource = DebugSummaryRemoteDataSource(
			apiRemoteDataSource = remoteDataSource,
			debugProfilePictureStorageDataSource = storageDataSource
		)

		dataSource.removeProfilePicture()

		assertEquals(1, remoteDataSource.removeCalls)
		assertEquals(1, storageDataSource.clearCalls)
	}

	private class RecordingRemoteDataSource(
		private val user: User = DEFAULT_SUMMARY_USER,
		private val profilePicture: ProfilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE
	) : RemoteDataSource {
		val uploadCalls = mutableListOf<Pair<ByteArray, String>>()
		var removeCalls = 0

		override suspend fun getUser(): User = user

		override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
			uploadCalls += content to mimeType
			return profilePicture
		}

		override suspend fun removeProfilePicture() {
			removeCalls++
		}
	}

	private class FakeDebugProfilePictureStorageDataSource(
		localPictureUrl: String? = null,
		storedRemoteUrl: String? = null
	) : DebugProfilePictureStorageDataSource {
		var savedContent: ByteArray? = null
		var savedRemoteUrl: String? = null
		var clearCalls = 0
		private var localPictureUrl: String? = localPictureUrl
		private var storedRemoteUrl: String? = storedRemoteUrl

		override suspend fun saveProfilePicture(content: ByteArray, remoteUrl: String): String {
			savedContent = content
			savedRemoteUrl = remoteUrl
			storedRemoteUrl = remoteUrl
			localPictureUrl = "file:///debug/profile-picture.img"
			return localPictureUrl!!
		}

		override suspend fun getLocalProfilePictureUrl(remoteUrl: String): String? {
			return if (storedRemoteUrl == remoteUrl) localPictureUrl else null
		}

		override suspend fun clearProfilePicture() {
			clearCalls++
			localPictureUrl = null
			storedRemoteUrl = null
		}
	}
}
