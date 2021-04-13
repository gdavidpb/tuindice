package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SignInError {
    object Timeout : SignInError()
    object InvalidCredentials : SignInError()
    object OutdatedPassword : SignInError()
    object AccountDisabled : SignInError()
    class NoConnection(val isNetworkAvailable: Boolean) : SignInError()
}