package com.gdavidpb.tuindice.login.domain.usecase.error

sealed class SignInError {
	object Timeout : SignInError()
	object InvalidCredentials : SignInError()
	object EmptyUsbId : SignInError()
	object InvalidUsbId : SignInError()
	object EmptyPassword : SignInError()
	object AccountDisabled : SignInError()
	object Unavailable : SignInError()
	class NoConnection(val isNetworkAvailable: Boolean) : SignInError()
}