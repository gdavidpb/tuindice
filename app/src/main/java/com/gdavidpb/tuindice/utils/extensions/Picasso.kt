package com.gdavidpb.tuindice.utils.extensions

import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.RequestCreator

fun RequestCreator.into(imageView: ImageView, liveResult: LiveCompletable<Any>) { //todo define error
    liveResult.postLoading()

    return into(imageView, object : Callback {
        override fun onSuccess() {
            liveResult.postComplete()
        }

        override fun onError(e: Exception?) {
            liveResult.postError(e ?: NullPointerException("e"))
        }
    })
}