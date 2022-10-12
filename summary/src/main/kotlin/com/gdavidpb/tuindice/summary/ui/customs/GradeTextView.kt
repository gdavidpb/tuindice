package com.gdavidpb.tuindice.summary.ui.customs

import android.animation.ValueAnimator
import android.content.Context
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.summary.R
import com.google.android.material.textview.MaterialTextView

class GradeTextView(context: Context, attrs: AttributeSet) : MaterialTextView(context, attrs) {

	private object Defaults {
		const val DECIMALS = 2
		const val GRADE = 0f

		const val TIME_ANIMATION = 750L
	}

	private val relativeSizeSpan = RelativeSizeSpan(0.5f)

	private val gradeDecimals: Int
	private val maxGradeDecimals: Int

	var grade: Float = Defaults.GRADE
		set(value) {
			field = value
			updateText()
		}

	var maxGrade: Float = Defaults.GRADE
		set(value) {
			field = value
			updateText()
		}

	init {
		loadAttributes(R.styleable.GradeTextView, attrs).apply {
			gradeDecimals = getInt(R.styleable.GradeTextView_gradeDecimals, Defaults.DECIMALS)
			maxGradeDecimals = getInt(R.styleable.GradeTextView_maxGradeDecimals, Defaults.DECIMALS)

			grade = getFloat(R.styleable.GradeTextView_grade, Defaults.GRADE)
			maxGrade = getFloat(R.styleable.GradeTextView_maxGrade, Defaults.GRADE)
		}.recycle()

		updateText()
	}

	fun animateGrade(value: Float) {
		if (grade == value) return

		ValueAnimator.ofFloat(grade, value)
			.duration(Defaults.TIME_ANIMATION)
			.interpolator(DecelerateInterpolator())
			.doOnUpdate<Float> { grade = it }
			.start()
	}

	private fun updateText() {
		text = buildSpannedString {
			val gradeText = grade.formatGrade(gradeDecimals)
			val maxGradeText = maxGrade.formatGrade(maxGradeDecimals)

			append(gradeText)
			append("/$maxGradeText", relativeSizeSpan)
		}
	}
}