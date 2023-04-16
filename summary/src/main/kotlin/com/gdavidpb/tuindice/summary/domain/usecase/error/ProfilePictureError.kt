package com.gdavidpb.tuindice.summary.domain.usecase.error

sealed class ProfilePictureError {
	object Timeout : ProfilePictureError()
	object IO : ProfilePictureError()
	class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureError()
}