package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SignInError {
    object Timeout : SignInError()
    object InvalidCredentials : SignInError()
    object AccountDisabled : SignInError()
    object Unavailable : SignInError()
    class NoConnection(val isNetworkAvailable: Boolean) : SignInError()
}