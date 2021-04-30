package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.gdavidpb.tuindice.utils.DECIMALS_DIV

class EvaluationGradeSeekBar(context: Context, attrs: AttributeSet)
    : AppCompatSeekBar(context, attrs) {

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

    private fun Double.toProgress() = div(DECIMALS_DIV).toInt()

    private fun Int.toGrade() = times(DECIMALS_DIV)
}