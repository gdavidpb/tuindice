package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SendVerificationEmailError {
    object AccountDisabled : SendVerificationEmailError()
    class NoConnection(val isNetworkAvailable: Boolean) : SendVerificationEmailError()
}
