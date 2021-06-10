package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SyncError {
    object Timeout : SyncError()
    object Unavailable : SyncError()
    object AccountDisabled : SyncError()
    object OutdatedPassword : SyncError()
    class NoConnection(val isNetworkAvailable: Boolean) : SyncError()
}