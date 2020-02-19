package com.gdavidpb.tuindice.ui.customs

import android.widget.TextView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.NO_SETTER
import com.gdavidpb.tuindice.utils.mappers.formatEvaluationDate
import java.util.*
import kotlin.DeprecationLevel.ERROR

class EvaluationDatePicker(private val textView: TextView) {
    var selectedDate = Date(0)
        get() = if (isDateSelectable) field else Date(0)
        set(value) {
            field = value

            updateText()
        }

    var isDateSelectable: Boolean = true
        set(value) {
            field = value

            textView.isEnabled = value

            updateText()
        }

    @Suppress("UNUSED_PARAMETER")
    var isValidState
        get() = !isDateSelectable || selectedDate.time != 0L
        @Deprecated(message = NO_SETTER, level = ERROR) set(value) = throw NotImplementedError()

    private fun updateText() {
        val context = textView.context

        textView.text = when {
            !isDateSelectable -> context.getString(R.string.label_evaluation_no_date)
            selectedDate.time != 0L -> selectedDate.formatEvaluationDate()
            else -> context.getString(R.string.label_evaluation_select_date)
        }
    }
}
