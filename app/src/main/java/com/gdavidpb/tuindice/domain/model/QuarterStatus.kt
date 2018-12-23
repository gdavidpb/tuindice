package com.gdavidpb.tuindice.domain.model

import android.content.Context
import androidx.annotation.ColorRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.getCompatColor

enum class QuarterStatus(
        val value: Int,
        @ColorRes private val color: Int
) {
    NONE(-1, 0),
    ALL(0, 0),
    CURRENT(1, R.color.quarterCurrent),
    COMPLETED(2, R.color.quarterCompleted),
    GUESS(3, R.color.quarterGuess),
    RETIRED(4, R.color.quarterRetired);

    fun getColor(context: Context) = context.getCompatColor(color)

    companion object {
        fun fromValue(value: Int): QuarterStatus = when (value) {
            ALL.value -> ALL
            CURRENT.value -> CURRENT
            COMPLETED.value -> COMPLETED
            GUESS.value -> GUESS
            RETIRED.value -> RETIRED
            else -> NONE
        }
    }
}