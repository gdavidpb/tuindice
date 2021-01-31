package com.gdavidpb.tuindice.domain.usecase.errors

sealed class StartUpError {
    object InvalidLink : StartUpError()
    object UnableToStart : StartUpError()
    object AccountDisabled : StartUpError()
    object NoConnection : StartUpError()
}
