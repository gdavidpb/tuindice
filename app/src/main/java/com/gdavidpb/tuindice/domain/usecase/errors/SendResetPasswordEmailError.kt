package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SendResetPasswordEmailError {
    object AccountDisabled : SendResetPasswordEmailError()
    object NoConnection : SendResetPasswordEmailError()
}
