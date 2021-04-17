package com.gdavidpb.tuindice.domain.usecase.errors

sealed class UpdatePasswordError {
    object Timeout : UpdatePasswordError()
    object InvalidCredentials : UpdatePasswordError()
    class NoConnection(val isNetworkAvailable: Boolean) : UpdatePasswordError()
}
