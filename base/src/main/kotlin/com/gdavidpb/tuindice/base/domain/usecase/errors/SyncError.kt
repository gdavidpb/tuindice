package com.gdavidpb.tuindice.base.domain.usecase.errors

sealed class SyncError {
    object Timeout : SyncError()
    object Unavailable : SyncError()
    object AccountDisabled : SyncError()
    object OutdatedPassword : SyncError()
    object IllegalAuthProvider : SyncError()
    class NoConnection(val isNetworkAvailable: Boolean) : SyncError()
}