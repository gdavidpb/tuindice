package com.gdavidpb.tuindice.summary.data.source.user

import com.gdavidpb.tuindice.summary.data.contract.user.DebugProfilePictureStorageDataSource
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write

class FileKitDebugProfilePictureStorageDataSource(
	private val sourceName: String
) : DebugProfilePictureStorageDataSource {
	private val debugProfilePictureDir = FileKit.filesDir / "summaryDebug"
	private val debugProfilePictureFile = debugProfilePictureDir / "$sourceName-profile-picture.img"
	private var storedRemoteUrl: String? = null
	private var storedLocalUrl: String? = null

	override suspend fun saveProfilePicture(content: ByteArray, remoteUrl: String): String {
		debugProfilePictureDir.createDirectories()

		if (debugProfilePictureFile.exists()) {
			debugProfilePictureFile.delete(mustExist = false)
		}

		debugProfilePictureFile.write(content)

		return debugProfilePictureFile.path.also { localUrl ->
			storedRemoteUrl = remoteUrl
			storedLocalUrl = localUrl
		}
	}

	override suspend fun getLocalProfilePictureUrl(remoteUrl: String): String? {
		if (storedRemoteUrl != remoteUrl || !debugProfilePictureFile.exists()) return null

		return storedLocalUrl ?: debugProfilePictureFile.path.also { path ->
			storedLocalUrl = path
		}
	}

	override suspend fun clearProfilePicture() {
		if (debugProfilePictureFile.exists()) {
			debugProfilePictureFile.delete(mustExist = false)
		}

		storedRemoteUrl = null
		storedLocalUrl = null
	}
}
