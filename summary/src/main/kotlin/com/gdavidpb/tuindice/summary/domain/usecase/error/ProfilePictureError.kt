package com.gdavidpb.tuindice.summary.domain.usecase.error

sealed class ProfilePictureError {
	object Timeout : ProfilePictureError()
	object InvalidSource : ProfilePictureError()
	class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureError()
}