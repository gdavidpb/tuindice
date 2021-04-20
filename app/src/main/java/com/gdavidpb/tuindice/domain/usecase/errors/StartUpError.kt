package com.gdavidpb.tuindice.domain.usecase.errors

sealed class StartUpError {
    object InvalidLink : StartUpError()
    class NoConnection(val isNetworkAvailable: Boolean) : StartUpError()
}
