package com.gdavidpb.tuindice.login.ui.customs

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.login.R
import kotlinx.android.synthetic.main.view_animated_background.view.*

class AnimatedBackgroundView(context: Context, attrs: AttributeSet) :
	ConstraintLayout(context, attrs) {

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