package com.gdavidpb.tuindice.summary.ui.model

import com.gdavidpb.tuindice.summary.presentation.mapper.profilePictureIdentity

data class ProfilePictureDisplay(
	val url: String,
	val cacheVersion: Int = 0,
	val localPreviewPath: String? = null,
	val isUploading: Boolean = false
) {
	val identity: String get() = profilePictureIdentity(url)
}
