package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SyncError {
    object IllegalState : SyncError()
    object AccountDisabled : SyncError()
    @Deprecated("Rename to CredentialsChanged")
    object InvalidCredentials : SyncError()
    class NoConnection(val isNetworkAvailable: Boolean) : SyncError()
}