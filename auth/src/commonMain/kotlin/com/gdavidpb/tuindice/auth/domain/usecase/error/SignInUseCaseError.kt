package com.gdavidpb.tuindice.auth.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface SignInUseCaseError : UseCaseError {
	data object Timeout : SignInUseCaseError
	data object InvalidCredentials : SignInUseCaseError
	data object EmptyUsbId : SignInUseCaseError
	data object InvalidUsbId : SignInUseCaseError
	data object EmptyPassword : SignInUseCaseError
	data object AccountDisabled : SignInUseCaseError
	data object Untrusted : SignInUseCaseError
	data object Unavailable : SignInUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : SignInUseCaseError
}
