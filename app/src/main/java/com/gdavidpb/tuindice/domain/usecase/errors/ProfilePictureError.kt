package com.gdavidpb.tuindice.domain.usecase.errors

sealed class ProfilePictureError {
    object Timeout : ProfilePictureError()
    object IO : ProfilePictureError()
    object NoData : ProfilePictureError()
    object AccountDisabled : ProfilePictureError()
    class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureError()
}