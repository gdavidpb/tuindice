package com.gdavidpb.tuindice.summary.ui.customs

import android.content.Context
import android.net.ConnectivityManager
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import kotlinx.android.synthetic.main.view_profile_picture.view.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException

class ProfilePictureView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs),
	KoinComponent {

	private val picasso by inject<Picasso>()
	private val connectivityManager by inject<ConnectivityManager>()

	private var _hasProfilePicture = false
	val hasProfilePicture: Boolean
		get() = _hasProfilePicture

	init {
		inflate(context, R.layout.view_profile_picture, this)
	}

	fun setLoading(value: Boolean) {
		if (value) {
			pBarProfile.isVisible = true

			iViewProfile.isEnabled = false
			iViewEditProfile.isEnabled = false
			iViewProfile.animateScaleDown()
			iViewEditProfile.animateScaleDown()
		} else {
			pBarProfile.isInvisible = true

			iViewProfile.isEnabled = true
			iViewEditProfile.isEnabled = true
			iViewProfile.animateScaleUp()
			iViewEditProfile.animateScaleUp()
		}
	}

	fun loadDefaultProfilePicture() {
		_hasProfilePicture = false
		iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
	}

	fun loadProfilePicture(
		url: String,
		invalidate: Boolean,
		liveData: LiveCompletable<ProfilePictureError>
	) {
		if (url.isNotEmpty()) {
			with(picasso) {
				if (invalidate) invalidate(url)

				load(url)
					.noFade()
					.stableKey(url)
					.error(R.mipmap.ic_launcher_round)
					.into(iViewProfile, liveData)
			}
		} else {
			liveData.postError(ProfilePictureError.NoData)
		}
	}

	private fun RequestCreator.into(
		imageView: ImageView,
		liveResult: LiveCompletable<ProfilePictureError>
	) {
		liveResult.postLoading()

		return into(imageView, object : Callback {
			override fun onSuccess() {
				_hasProfilePicture = true
				liveResult.postComplete()
			}

			override fun onError(e: Exception) {
				_hasProfilePicture = false
				val error = when {
					e is IOException -> ProfilePictureError.IO
					e.isConnection() -> ProfilePictureError.NoConnection(connectivityManager.isNetworkAvailable())
					else -> null
				}

				liveResult.postError(error)
			}
		})
	}
}