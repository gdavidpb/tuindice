package com.gdavidpb.tuindice.summary.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import coil.load
import coil.transform.CircleCropTransformation
import com.gdavidpb.tuindice.base.utils.extension.animateScaleDown
import com.gdavidpb.tuindice.base.utils.extension.animateScaleUp
import com.gdavidpb.tuindice.summary.R
import kotlinx.android.synthetic.main.view_profile_picture.view.iViewEditProfile
import kotlinx.android.synthetic.main.view_profile_picture.view.iViewProfile
import kotlinx.android.synthetic.main.view_profile_picture.view.pBarProfile

class ProfilePictureView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

	var hasProfilePicture: Boolean = false
		private set

	private var profilePictureUrl: String? = null

	init {
		inflate(context, R.layout.view_profile_picture, this)
	}

	override fun setOnClickListener(l: OnClickListener?) {
		iViewProfile.setOnClickListener(l)
		iViewEditProfile.setOnClickListener(l)
	}

	fun loadImage(url: String, lifecycleOwner: LifecycleOwner) {
		if (profilePictureUrl == url) return

		profilePictureUrl = url

		hasProfilePicture = url.isNotEmpty()

		val data = url.ifEmpty { null }

		iViewProfile.load(data) {
			lifecycle(lifecycleOwner)
			error(R.mipmap.ic_launcher_round)
			placeholder(R.mipmap.ic_launcher_round)
			transformations(CircleCropTransformation())

			listener(
				onStart = { showLoading(true) },
				onCancel = { showLoading(false) },
				onError = { _, _ -> showLoading(false) },
				onSuccess = { _, _ -> showLoading(false) }
			)
		}
	}

	fun showLoading(value: Boolean) {
		if (pBarProfile.isVisible == value) return

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
}