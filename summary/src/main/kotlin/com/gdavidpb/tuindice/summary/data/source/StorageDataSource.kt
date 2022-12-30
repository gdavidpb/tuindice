package com.gdavidpb.tuindice.summary.data.source

interface StorageDataSource {
	suspend fun getProfilePicture(uid: String): String
	suspend fun existsProfilePicture(uid: String): Boolean
	suspend fun downloadProfilePicture(uid: String, url: String): String

	suspend fun clear()
}