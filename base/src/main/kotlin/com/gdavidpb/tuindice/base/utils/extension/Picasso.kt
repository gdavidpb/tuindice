package com.gdavidpb.tuindice.base.utils.extension

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target

fun RequestCreator.into(resources: Resources, liveData: LiveResult<Drawable, Nothing>) {
	into(object : Target {
		override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
			val drawable = bitmap.toDrawable(resources)

			liveData.postSuccess(drawable)
		}

		override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
			liveData.postError(null)
		}

		override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
			liveData.postLoading()
		}
	})
}