package com.gdavidpb.tuindice.summary.ui.model

data class ProfilePictureDisplay(
	val url: String,
	val cacheVersion: Int = 0,
	val localPreviewPath: String? = null,
	val isUploading: Boolean = false
)
