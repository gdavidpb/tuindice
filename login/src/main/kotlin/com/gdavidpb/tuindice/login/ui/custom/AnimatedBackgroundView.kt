package com.gdavidpb.tuindice.login.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdavidpb.tuindice.base.utils.extension.doOnUpdate
import com.gdavidpb.tuindice.base.utils.extension.duration
import com.gdavidpb.tuindice.base.utils.extension.getDrawable
import com.gdavidpb.tuindice.base.utils.extension.getEnum
import com.gdavidpb.tuindice.base.utils.extension.interpolator
import com.gdavidpb.tuindice.base.utils.extension.loadAttributes
import com.gdavidpb.tuindice.base.utils.extension.repeatCount
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.login.R

class AnimatedBackgroundView(context: Context, attrs: AttributeSet) :
	ConstraintLayout(context, attrs) {

	private val backgroundOne by view<BackgroundImageView>(R.id.backgroundOne)
	private val backgroundTwo by view<BackgroundImageView>(R.id.backgroundTwo)

	private val backgroundImage: Drawable?
	private val scaleType: ImageView.ScaleType

	init {
		inflate(context, R.layout.view_animated_background, this)

		loadAttributes(R.styleable.AnimatedBackgroundView, attrs).apply {
			backgroundImage = getDrawable(context, R.styleable.AnimatedBackgroundView_background, 0)
			scaleType =
				getEnum(R.styleable.AnimatedBackgroundView_scaleType, ImageView.ScaleType.FIT_XY)
		}.recycle()

		val width = Resources.getSystem().displayMetrics.widthPixels

		backgroundOne.scaleType = scaleType
		backgroundTwo.scaleType = scaleType
		backgroundOne.background = backgroundImage
		backgroundTwo.background = backgroundImage

		ValueAnimator.ofInt(0, width)
			.duration(30000L)
			.repeatCount(ValueAnimator.INFINITE)
			.interpolator(LinearInterpolator())
			.doOnUpdate<Float> { translation ->
				backgroundOne.translationX = translation
				backgroundTwo.translationX = (translation - width)
			}
			.start()
	}
}