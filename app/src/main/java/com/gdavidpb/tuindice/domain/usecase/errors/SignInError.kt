package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SignInError {
    object InvalidCredentials : SignInError()
    object AccountDisabled : SignInError()
    object NoConnection : SignInError()
}