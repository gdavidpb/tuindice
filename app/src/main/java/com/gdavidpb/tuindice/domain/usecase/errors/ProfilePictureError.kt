package com.gdavidpb.tuindice.domain.usecase.errors

sealed class ProfilePictureError {
    object IO : ProfilePictureError()
    object NoConnection : ProfilePictureError()
}