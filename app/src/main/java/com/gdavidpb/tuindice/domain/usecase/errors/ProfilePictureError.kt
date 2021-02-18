package com.gdavidpb.tuindice.domain.usecase.errors

sealed class ProfilePictureError {
    object IO : ProfilePictureError()
    object NoData : ProfilePictureError()
    class NoConnection(val isNetworkAvailable: Boolean) : ProfilePictureError()
}