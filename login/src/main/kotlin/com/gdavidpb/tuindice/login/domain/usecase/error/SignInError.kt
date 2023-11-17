package com.gdavidpb.tuindice.login.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class SignInError : Error {
	data object Timeout : SignInError()
	data object InvalidCredentials : SignInError()
	data object EmptyUsbId : SignInError()
	data object InvalidUsbId : SignInError()
	data object EmptyPassword : SignInError()
	data object AccountDisabled : SignInError()
	data object Unavailable : SignInError()
	class NoConnection(val isNetworkAvailable: Boolean) : SignInError()
}