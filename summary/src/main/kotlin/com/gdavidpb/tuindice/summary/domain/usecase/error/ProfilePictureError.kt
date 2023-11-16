package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class ProfilePictureError : Error {
	data object Timeout : ProfilePictureError()
	data object InvalidSource : ProfilePictureError()
	class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureError()
}