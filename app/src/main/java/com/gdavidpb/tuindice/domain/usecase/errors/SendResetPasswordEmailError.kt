package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SendResetPasswordEmailError {
    object AccountDisabled : SendResetPasswordEmailError()
    class NoConnection(val isNetworkAvailable: Boolean) : SendResetPasswordEmailError()
}
