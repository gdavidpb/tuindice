package com.gdavidpb.tuindice.ui.customs

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.customs.graphs.extensions.getDrawable
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.view_animated_background.view.*

class AnimatedBackgroundView(context: Context, attrs: AttributeSet) :
	ConstraintLayout(context, attrs) {

	private val backgroundImage: Drawable?

	init {
		inflate(context, R.layout.view_animated_background, this)

		loadAttributes(R.styleable.AnimatedBackgroundView, attrs).apply {
			backgroundImage = getDrawable(context, R.styleable.AnimatedBackgroundView_backgroundResource, 0)
		}.recycle()

		val width = Resources.getSystem().displayMetrics.widthPixels

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