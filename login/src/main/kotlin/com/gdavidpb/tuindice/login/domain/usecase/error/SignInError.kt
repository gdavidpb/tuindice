package com.gdavidpb.tuindice.login.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class SignInError : Error {
	object Timeout : SignInError()
	object InvalidCredentials : SignInError()
	object EmptyUsbId : SignInError()
	object InvalidUsbId : SignInError()
	object EmptyPassword : SignInError()
	object AccountDisabled : SignInError()
	object Unavailable : SignInError()
	class NoConnection(val isNetworkAvailable: Boolean) : SignInError()
}