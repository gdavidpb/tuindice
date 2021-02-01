package com.gdavidpb.tuindice.utils.extensions

import android.widget.ImageView
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator
import java.io.IOException

fun RequestCreator.into(imageView: ImageView, liveResult: LiveCompletable<ProfilePictureError>) {
    liveResult.postLoading()

    return into(imageView, object : Callback {
        override fun onSuccess() {
            liveResult.postComplete()
        }

        override fun onError(e: Exception) {
            val error = when {
                e is IOException -> ProfilePictureError.IO
                e.isConnectionIssue() -> ProfilePictureError.NoConnection
                else -> null
            }

            liveResult.postError(error)
        }
    })
}