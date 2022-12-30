package com.gdavidpb.tuindice.summary.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.base.utils.extensions.copyToAndClose
import com.gdavidpb.tuindice.summary.data.source.StorageDataSource
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

class InternalStorageDataSource(
	private val context: Context
) : StorageDataSource {

	private val root by lazy { context.filesDir }
	private val profilePictures by lazy { File(root, "profile_pictures") }

	private val profilePictureFile = { uid: String -> File(profilePictures, "$uid.jpg") }

	override suspend fun getProfilePicture(uid: String): String {
		val profilePictureFile = profilePictureFile(uid)

		return profilePictureFile.path
	}

	override suspend fun existsProfilePicture(uid: String): Boolean {
		val profilePictureFile = profilePictureFile(uid)

		return profilePictureFile.exists()
	}

	override suspend fun downloadProfilePicture(uid: String, url: String): String {
		val profilePictureFile = profilePictureFile(uid)

		profilePictureFile.outputStream().use { outputStream ->
			URL(url).openStream().copyToAndClose(outputStream)
		}

		return profilePictureFile.path
	}

	override suspend fun clear() {
		runCatching {
			root.deleteRecursively()
			root.mkdir()
		}.onFailure { throwable ->
			if (throwable !is FileNotFoundException) throw throwable
		}
	}
}