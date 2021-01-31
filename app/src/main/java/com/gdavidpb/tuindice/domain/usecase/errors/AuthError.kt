package com.gdavidpb.tuindice.domain.usecase.errors

sealed class AuthError {
    object InvalidCredentials : AuthError()
}