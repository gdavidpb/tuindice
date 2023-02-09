package com.gdavidpb.tuindice.summary.domain.error

sealed class ProfilePictureError {
	object Timeout : ProfilePictureError()
	object IO : ProfilePictureError()
	class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureError()
}