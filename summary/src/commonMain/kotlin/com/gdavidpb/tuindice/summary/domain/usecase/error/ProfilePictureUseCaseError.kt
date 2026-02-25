package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface ProfilePictureUseCaseError : UseCaseError {
	data object Timeout : ProfilePictureUseCaseError
	data object InvalidSource : ProfilePictureUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureUseCaseError
}