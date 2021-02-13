package com.gdavidpb.tuindice.domain.usecase.errors

sealed class ProfilePictureError {
    object IO : ProfilePictureError()
    object NoData : ProfilePictureError()
    object NoConnection : ProfilePictureError()
}