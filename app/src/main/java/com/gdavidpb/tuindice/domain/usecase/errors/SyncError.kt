package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SyncError {
    object NoAuthenticated : SyncError()
    object NoDataAvailable : SyncError()
    object AccountDisabled : SyncError()
    class NoConnection(val isNetworkAvailable: Boolean) : SyncError()
}