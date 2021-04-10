package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SyncError {
    object Unauthenticated : SyncError()
    object AccountDisabled : SyncError()
    object InvalidCredentials : SyncError()
    class NoConnection(val isNetworkAvailable: Boolean) : SyncError()
}