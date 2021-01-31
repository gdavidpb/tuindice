package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SendVerificationEmailError {
    object AccountDisabled : SendVerificationEmailError()
    object NoConnection : SendVerificationEmailError()
}
