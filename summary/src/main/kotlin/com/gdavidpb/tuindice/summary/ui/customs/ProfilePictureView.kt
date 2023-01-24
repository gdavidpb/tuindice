package com.gdavidpb.tuindice.summary.ui.customs

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.summary.R
import kotlinx.android.synthetic.main.view_profile_picture.view.*

class ProfilePictureView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

	var hasProfilePicture: Boolean = false
		private set

	init {
		inflate(context, R.layout.view_profile_picture, this)
	}

	override fun setOnClickListener(l: OnClickListener?) {
		iViewProfile.setOnClickListener(l)
		iViewEditProfile.setOnClickListener(l)
	}

	fun setDrawable(drawable: Drawable?) {
		if (drawable != null) {
			hasProfilePicture = true
			iViewProfile.setImageDrawable(drawable)
		} else {
			hasProfilePicture = false
			iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
		}
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
}