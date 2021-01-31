package com.gdavidpb.tuindice.domain.usecase.errors

sealed class SyncError {
    object NoAuthenticated : SyncError()
    object NoDataAvailable : SyncError()
    object NoSynced : SyncError()
    object AccountDisabled : SyncError()
    object NoConnection : SyncError()
}