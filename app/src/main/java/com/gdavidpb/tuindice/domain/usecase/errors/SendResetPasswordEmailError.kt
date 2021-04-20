package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SendResetPasswordEmailError {
    object Timeout : SendResetPasswordEmailError()
    class NoConnection(val isNetworkAvailable: Boolean) : SendResetPasswordEmailError()
}
