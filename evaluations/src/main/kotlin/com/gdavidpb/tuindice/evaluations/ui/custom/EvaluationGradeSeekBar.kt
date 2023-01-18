package com.gdavidpb.tuindice.evaluations.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.gdavidpb.tuindice.base.utils.MIN_EVALUATION_GRADE

class EvaluationGradeSeekBar(context: Context, attrs: AttributeSet) :
	AppCompatSeekBar(context, attrs) {

	var maxGrade: Double = max.toGrade()
		get() = max.toGrade()
		set(value) {
			field = value

			max = value.toProgress()
		}

	var grade: Double = progress.toGrade()
		get() = progress.toGrade()
		set(value) {
			field = value

			progress = value.toProgress()
		}

	private fun Double.toProgress() = div(MIN_EVALUATION_GRADE).toInt()

	private fun Int.toGrade() = times(MIN_EVALUATION_GRADE)
}