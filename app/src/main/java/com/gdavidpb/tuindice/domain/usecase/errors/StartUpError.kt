package com.gdavidpb.tuindice.domain.usecase.errors

sealed class StartUpError {
    object InvalidLink : StartUpError()
    object UnableToStart : StartUpError()
    object AccountDisabled : StartUpError()
    class NoConnection(val isNetworkAvailable: Boolean) : StartUpError()
}
